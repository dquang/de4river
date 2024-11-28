#!/bin/bash
# Import script for rivers
#
# Authors:
# Andre Heinecke <aheinecke@intevation.de>
#
# Copyright:
# Copyright (C) 2013 Intevation GmbH
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

set -e

# Default settings
DEFAULT_HOST=localhost
DEFAULT_PORT=1521
DEFAULT_USER=flys_dami
DEFAULT_PASS=flys_dami
DEFAULT_LOG=$PWD/logs
DEFAULT_BACKEND_NAME="XE"
JAR="bin/river-backend-1.0-SNAPSHOT*.jar"
IMPORTER_DRY_RUN=false
IMPORTER_MAINVALUE_TYPES=QWTD-
IMPORTER_ANNOTATION_TYPES="conf/annotation-types.xml"


MIN_MEMORY="8024m"

# Default encoding. Change here if necessary
export LC_ALL=de_DE@euro

usage(){
    cat << EOF

usage: $0 [options] gew_file

Import a river described by the gew_file

OPTIONS:
   -?, --help                      Show this message
   -u, --username=<username>       Database username. Default: $DEFAULT_USER
   -w, --password=<password>       Database password. Default: $DEFAULT_PASS
   -h, --host=<host>               Connect to database on host <host>.
                                   Default: $DEFAULT_HOST
   -p, --port=<number>             Use port number <number>. Default: $DEFAULT_PORT
   -d, --db-name=<database_name>   Name of the database / backend. Default: $DEFAULT_BACKEND_NAME
   -l, --log-dir=<directory>       Directory in which to create the log files.
                                   Default: $PWD/logs
   --postgres                      Database is PostgreSQL
   --skip-hydro                    Skip import of hydrological data
   --skip-morpho                   Skip import of morphological data
   --skip-geo                      Skip import of geographic data
   --skip-prf                      Skip import of cross section data
EOF
exit 0
}

OPTS=`getopt -o ?u:w:h:p:d:l: \
     -l help,username:,password:,host:,port:,db-name:,log-dir:,skip-hydro,skip-morpho,skip-geo,skip-prf,postgres \
     -n $0 -- "$@"`
if [ $? != 0 ] ; then usage; fi
eval set -- "$OPTS"
while true ; do
  case "$1" in
    "-?"|"--help")
      usage;;
    "--")
      shift
      break;;
    "-u"|"--username")
      DBUSER=$2
      shift 2;;
    "-w"|"--password")
      DBPASS=$2
      shift 2;;
    "-h"|"--host")
      DBHOST=$2
      shift 2;;
    "-p"|"--port")
      DBPORT=$2
      shift 2;;
    "-l"|"--log-dir")
      LOG=$2
      shift 2;;
    "-d"|"--db-name")
      BACKEND_NAME=$2
      shift 2;;
    "--skip-hydro")
      SKIP_HYDRO="TRUE"
      shift;;
    "--skip-morpho")
      SKIP_MORPHO="TRUE"
      shift;;
    "--skip-prf")
      SKIP_PRF="TRUE"
      shift;;
    "--skip-geo")
      SKIP_GEO="TRUE"
      shift;;
    "--postgres")
      POSTGRES="TRUE"
      shift;;
    *)
      echo "Unknown Option $1"
      usage;;
  esac
done

if [ -z $DBUSER ]; then
  DBUSER=$DEFAULT_USER
fi
if [ -z $DBPASS ]; then
  DBPASS=$DEFAULT_PASS
fi
if [ -z $DBPORT ]; then
  DBPORT=$DEFAULT_PORT
fi
if [ -z $DBHOST ]; then
  DBHOST=$DEFAULT_HOST
fi
if [ -z $BACKEND_NAME ]; then
  BACKEND_NAME=$DEFAULT_BACKEND_NAME
fi
if [ -z $LOG ]; then
  LOG=$DEFAULT_LOG
fi

if [ $# != 1 ]; then
    usage
fi

if [ ! -r $1 ]; then
    echo "Could not open $1 please ensure it exists and is readable"
fi

GEW_FILE="$1"
RIVER_NAME=$(grep "Gew.sser" "$1" | sed 's/Gew.sser: //')
DATE=$(date +%Y.%m.%d_%H%M)
LOG_DIR=${LOG}/`basename $GEW_FILE .gew`-$DATE
mkdir -p ${LOG_DIR}

cat > "$LOG_DIR/log4j2.xml" << "EOF"
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <RollingFile
            name="RollingFile"
            fileName="import.log"
            filePattern="import.log.%i">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5p %c - %m%n"/>
            <SizeBasedTriggeringPolicy size="100000 KB"/>
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>
    </Appenders>
    <Loggers>
        <Root level="DEBUG">
            <AppenderRef ref="RollingFile"/>
        </Root>
    </Loggers>
</Configuration>
EOF

if [ "$POSTGRES" = "TRUE" ]; then
    JAR=$(echo "$JAR" | sed 's/importer/importer_psql/')
    if [ ! -r "$JAR" ]; then
      echo "Could not find Postgres importer $JAR"
      exit 1
    fi
    OGR_CONNECTION="PG:dbname=$BACKEND_NAME host=$DBHOST port=$DBPORT \
      user=$DBUSER password=$DBPASS"
    BACKEND_DB_PREFIX="jdbc:postgresql:"
    BACKEND_DB_DRIVER="org.postgresql.Driver"
    BACKEND_DB_DIALECT="org.hibernate.dialect.PostgreSQLDialect"
else
    BACKEND_DB_PREFIX="jdbc:oracle:thin:@"
    BACKEND_DB_DRIVER="oracle.jdbc.OracleDriver"
    BACKEND_DB_DIALECT="org.hibernate.dialect.OracleDialect"
fi

BACKEND_URL=$BACKEND_DB_PREFIX//$DBHOST:$DBPORT/$BACKEND_NAME

echo "Importing $RIVER_NAME into $BACKEND_URL."

import_hydro(){
    LOG_FILE=${LOG_DIR}/hydro.log
    echo Importing Hydrological data.
    echo Logging into: $LOG_FILE
    sed -i "s/import.log/$LOG_FILE/" $LOG_DIR/log4j2.xml
    java -jar \
    -Xmx$MIN_MEMORY \
    -server \
    -Dlog4j2.configurationFile=file://$LOG_DIR/log4j2.xml \
    -Dflys.backend.user=$DBUSER \
    -Dflys.backend.password=$DBPASS \
    -Dflys.backend.url=$BACKEND_URL \
    -Dflys.backend.driver=$BACKEND_DB_DRIVER \
    -Dflys.backend.dialect=$BACKEND_DB_DIALECT \
    -Dflys.backend.importer.infogew.file="$GEW_FILE" \
    -Dflys.backend.main.value.types=$IMPORTER_MAINVALUE_TYPES \
    -Dflys.backend.importer.annotation.types=$IMPORTER_ANNOTATION_TYPES \
    -Dflys.backend.importer.dry.run=$IMPORTER_DRY_RUN \
    -Dflys.backend.importer.skip.annotations=false \
    -Dflys.backend.importer.skip.bwastr=false \
    -Dflys.backend.importer.skip.extra.wsts=false \
    -Dflys.backend.importer.skip.fixations=false \
    -Dflys.backend.importer.skip.flood.water=false \
    -Dflys.backend.importer.skip.flood.protection=false \
    -Dflys.backend.importer.skip.gauges=false \
    -Dflys.backend.importer.skip.historical.discharge.tables=true \
    -Dflys.backend.importer.skip.hyks=false \
    -Dflys.backend.importer.skip.official.lines=false \
    -Dflys.backend.importer.skip.prfs=true \
    -Dflys.backend.importer.skip.w80s=true \
    -Dflys.backend.importer.skip.w80.csvs=true \
    -Dflys.backend.importer.skip.da50s=true \
    -Dflys.backend.importer.skip.da66s=true \
    -Dflys.backend.importer.skip.wst=false \
    -Dflys.backend.importer.skip.measurement.stations=true \
    -Dflys.backend.importer.skip.waterlevel.differences=true \
    -Dflys.backend.importer.skip.waterlevels=true \
    -Dflys.backend.importer.skip.sq.relation=true \
    -Dflys.backend.importer.skip.sediment.density=true \
    -Dflys.backend.importer.skip.sediment.load=true \
    -Dflys.backend.importer.skip.sediment.load.ls=true \
    -Dflys.backend.importer.skip.morphological.width=true \
    -Dflys.backend.importer.skip.porosity=true \
    -Dflys.backend.importer.skip.flow.velocity=true \
    -Dflys.backend.importer.skip.bed.height=true \
    $JAR
}

import_morpho(){
    LOG_FILE=${LOG_DIR}/morpho.log
    echo Importing Morphological data.
    echo Logging into: $LOG_FILE
    sed -i "s/import.log/$LOG_FILE/" $LOG_DIR/log4j2.xml
    java -jar \
    -Xmx$MIN_MEMORY \
    -server \
    -Dlog4j2.configurationFile=file://$LOG_DIR/log4j2.xml \
    -Dflys.backend.user=$DBUSER \
    -Dflys.backend.password=$DBPASS \
    -Dflys.backend.url=$BACKEND_URL \
    -Dflys.backend.driver=$BACKEND_DB_DRIVER \
    -Dflys.backend.dialect=$BACKEND_DB_DIALECT \
    -Dflys.backend.importer.infogew.file="$GEW_FILE" \
    -Dflys.backend.main.value.types=$IMPORTER_MAINVALUE_TYPES \
    -Dflys.backend.importer.annotation.types=$IMPORTER_ANNOTATION_TYPES \
    -Dflys.backend.importer.dry.run=$IMPORTER_DRY_RUN \
    -Dflys.backend.importer.skip.annotations=true \
    -Dflys.backend.importer.skip.bwastr=true \
    -Dflys.backend.importer.skip.extra.wsts=true \
    -Dflys.backend.importer.skip.fixations=true \
    -Dflys.backend.importer.skip.flood.water=true \
    -Dflys.backend.importer.skip.flood.protection=true \
    -Dflys.backend.importer.skip.gauges=true \
    -Dflys.backend.importer.skip.historical.discharge.tables=true \
    -Dflys.backend.importer.skip.hyks=true \
    -Dflys.backend.importer.skip.official.lines=true \
    -Dflys.backend.importer.skip.prfs=true \
    -Dflys.backend.importer.skip.w80s=true \
    -Dflys.backend.importer.skip.w80.csvs=true \
    -Dflys.backend.importer.skip.da50s=true \
    -Dflys.backend.importer.skip.da66s=true \
    -Dflys.backend.importer.skip.wst=true \
    -Dflys.backend.importer.skip.measurement.stations=false \
    -Dflys.backend.importer.skip.waterlevel.differences=false \
    -Dflys.backend.importer.skip.waterlevels=false \
    -Dflys.backend.importer.skip.sq.relation=false \
    -Dflys.backend.importer.skip.sediment.density=false \
    -Dflys.backend.importer.skip.sediment.load=false \
    -Dflys.backend.importer.skip.sediment.load.ls=false \
    -Dflys.backend.importer.skip.morphological.width=false \
    -Dflys.backend.importer.skip.porosity=false \
    -Dflys.backend.importer.skip.flow.velocity=false \
    -Dflys.backend.importer.skip.bed.height=false \
    $JAR
}

import_prf(){
    LOG_FILE=${LOG_DIR}/prf.log
    echo Importing cross section data.
    echo Logging into: $LOG_FILE
    sed -i "s/import.log/$LOG_FILE/" $LOG_DIR/log4j2.xml
    java -jar \
    -Xmx$MIN_MEMORY \
    -server \
    -Dlog4j2.configurationFile=file://$LOG_DIR/log4j2.xml \
    -Dflys.backend.user=$DBUSER \
    -Dflys.backend.password=$DBPASS \
    -Dflys.backend.url=$BACKEND_URL \
    -Dflys.backend.driver=$BACKEND_DB_DRIVER \
    -Dflys.backend.dialect=$BACKEND_DB_DIALECT \
    -Dflys.backend.importer.infogew.file="$GEW_FILE" \
    -Dflys.backend.main.value.types=$IMPORTER_MAINVALUE_TYPES \
    -Dflys.backend.importer.annotation.types=$IMPORTER_ANNOTATION_TYPES \
    -Dflys.backend.importer.dry.run=$IMPORTER_DRY_RUN \
    -Dflys.backend.importer.skip.annotations=true \
    -Dflys.backend.importer.skip.bwastr=true \
    -Dflys.backend.importer.skip.extra.wsts=true \
    -Dflys.backend.importer.skip.fixations=true \
    -Dflys.backend.importer.skip.flood.water=true \
    -Dflys.backend.importer.skip.flood.protection=true \
    -Dflys.backend.importer.skip.gauges=true \
    -Dflys.backend.importer.skip.historical.discharge.tables=true \
    -Dflys.backend.importer.skip.hyks=true \
    -Dflys.backend.importer.skip.official.lines=true \
    -Dflys.backend.importer.skip.prfs=false \
    -Dflys.backend.importer.skip.w80s=false \
    -Dflys.backend.importer.skip.w80.csvs=false \
    -Dflys.backend.importer.skip.da50s=false \
    -Dflys.backend.importer.skip.da66s=false \
    -Dflys.backend.importer.skip.wst=true \
    -Dflys.backend.importer.skip.measurement.stations=true \
    -Dflys.backend.importer.skip.waterlevel.differences=true \
    -Dflys.backend.importer.skip.waterlevels=true \
    -Dflys.backend.importer.skip.sq.relation=true \
    -Dflys.backend.importer.skip.sediment.density=true \
    -Dflys.backend.importer.skip.sediment.load=true \
    -Dflys.backend.importer.skip.sediment.load.ls=true \
    -Dflys.backend.importer.skip.morphological.width=true \
    -Dflys.backend.importer.skip.porosity=true \
    -Dflys.backend.importer.skip.flow.velocity=true \
    -Dflys.backend.importer.skip.bed.height=true \
    $JAR
}

import_geo(){
    LOG_FILE=${LOG_DIR}/geo.log
    echo Importing Geographic data.
    echo Logging into: $LOG_FILE

    RIVER_PATH=$(grep "WSTDatei:" "$GEW_FILE" | awk '{print $2}')
    RIVER_PATH=$(dirname "$RIVER_PATH")/../..
    RIVER_PATH=$(readlink -f "$RIVER_PATH")

    exec python $(dirname $0)/shpimporter/shpimporter.py \
    --directory $RIVER_PATH \
    --river_name "$RIVER_NAME" \
    --ogr_connection "$OGR_CONNECTION" \
    --host $DBHOST/$BACKEND_NAME \
    --user $DBUSER \
    --password $DBPASS \
    --verbose 1 \
    > "$LOG_FILE" 2>&1
}


if [ "$SKIP_HYDRO" != "TRUE" ]; then
import_hydro
fi
if [ "$SKIP_PRF" != "TRUE" ]; then
import_prf
fi
if [ "$SKIP_MORPHO" != "TRUE" ]; then
import_morpho
fi
if [ "$SKIP_GEO" != "TRUE" ]; then
import_geo
fi
