#!/bin/bash

######################### CONFIG OPTIONS ############################
BACKEND_USER="flys28"
BACKEND_PASS="flys28"
BACKEND_HOST="czech-republic.atlas.intevation.de"
BACKEND_PORT="1521"
BACKEND_NAME="XE"
LOG4J_CONFIG="conf/log4j2.xml"
JAR="bin/river-backend-1.0-SNAPSHOT*.jar"
#####################################################################


########################## Oracle Settings ##########################
BACKEND_DB_PREFIX="jdbc:oracle:thin:@"
BACKEND_DB_DRIVER="oracle.jdbc.OracleDriver"
BACKEND_DB_DIALECT="org.hibernate.dialect.OracleDialect"
BACKEND_URL=$BACKEND_DB_PREFIX//$BACKEND_HOST:$BACKEND_PORT/$BACKEND_NAME
#####################################################################


######################## Custom Importer Settings ###################
IMPORTER_DRY_RUN=false
IMPORTER_MAINVALUE_TYPES=QWTD-
IMPORTER_ANNOTATION_TYPES="conf/annotation-types.xml"

IMPORTER_SKIP_ANNOTATIONS=false
IMPORTER_SKIP_BWASTR=false
IMPORTER_SKIP_DA50S=false
IMPORTER_SKIP_DA66S=false
IMPORTER_SKIP_EXTRA_WST=false
IMPORTER_SKIP_FIXATIONS=false
IMPORTER_SKIP_FLOOD_WATER=false
IMPORTER_SKIP_FLOOD_PROTECTION=false
IMPORTER_SKIP_GAUGES=false
IMPORTER_SKIP_HISTORICAL_DISCHARGE_GAUGES=true
IMPORTER_SKIP_HYKS=false
IMPORTER_SKIP_OFFICIAL_LINES=false
IMPORTER_SKIP_PRFS=false
IMPORTER_SKIP_W80S=false
IMPORTER_SKIP_W80_CSVS=false
IMPORTER_SKIP_WST=false

IMPORTER_SKIP_MEASUREMENT_STATIONS=false
IMPORTER_SKIP_BED_HEIGHT=false
IMPORTER_SKIP_FLOW_VELOCITY=false
IMPORTER_SKIP_MORPHOLOGICAL_WIDTH=false
IMPORTER_SKIP_POROSITY=false
IMPORTER_SKIP_SEDIMENT_DENSITY=false
IMPORTER_SKIP_SEDIMENT_LOAD=false
IMPORTER_SKIP_SEDIMENT_LOAD_LS=false
IMPORTER_SKIP_SQ_RELATION=false
IMPORTER_SKIP_WATERLEVELS=false
IMPORTER_SKIP_WATERLEVEL_DIFFERENCES=false
#####################################################################

MIN_MEMORY="8192m"

######################### Run Importer ##############################
INFO_GEW=$1

OPTIONAL_LIBS="${DIR}"/../opt
if [ -d "$OPTIONAL_LIBS" ]; then
    export PATH="$OPTIONAL_LIBS/bin:$PATH"
    export LD_LIBRARY_PATH="$OPTIONAL_LIBS/lib:$LD_LIBRARY_PATH"
    export LD_LIBRARY_PATH="$OPTIONAL_LIBS/lib64:$LD_LIBRARY_PATH"
fi

# Default encoding. Change here if necessary
export LC_ALL=de_DE@euro

exec java -jar \
    -Xmx$MIN_MEMORY \
    -server \
    -Dlog4j2.configurationFile=file://`readlink -f $LOG4J_CONFIG` \
    -Dflys.backend.importer.infogew.file=$INFO_GEW \
    -Dflys.backend.main.value.types=$IMPORTER_MAINVALUE_TYPES \
    -Dflys.backend.importer.annotation.types=$IMPORTER_ANNOTATION_TYPES \
    -Dflys.backend.importer.dry.run=$IMPORTER_DRY_RUN \
    -Dflys.backend.importer.skip.annotations=$IMPORTER_SKIP_ANNOTATIONS \
    -Dflys.backend.importer.skip.bed.height=$IMPORTER_SKIP_BED_HEIGHT \
    -Dflys.backend.importer.skip.bwastr=$IMPORTER_SKIP_BWASTR \
    -Dflys.backend.importer.skip.da50s=$IMPORTER_SKIP_DA50S \
    -Dflys.backend.importer.skip.da66s=$IMPORTER_SKIP_DA66S \
    -Dflys.backend.importer.skip.extra.wsts=$IMPORTER_SKIP_EXTRA_WST \
    -Dflys.backend.importer.skip.fixations=$IMPORTER_SKIP_FIXATIONS \
    -Dflys.backend.importer.skip.flood.water=$IMPORTER_SKIP_FLOOD_WATER \
    -Dflys.backend.importer.skip.flood.protection=$IMPORTER_SKIP_FLOOD_PROTECTION \
    -Dflys.backend.importer.skip.measurement.stations=$IMPORTER_SKIP_MEASUREMENT_STATIONS \
    -Dflys.backend.importer.skip.flow.velocity=$IMPORTER_SKIP_FLOW_VELOCITY \
    -Dflys.backend.importer.skip.gauges=$IMPORTER_SKIP_GAUGES \
    -Dflys.backend.importer.skip.historical.discharge.tables=$IMPORTER_SKIP_HISTORICAL_DISCHARGE_GAUGES \
    -Dflys.backend.importer.skip.hyks=$IMPORTER_SKIP_HYKS \
    -Dflys.backend.importer.skip.morphological.width=$IMPORTER_SKIP_MORPHOLOGICAL_WIDTH \
    -Dflys.backend.importer.skip.porosity=$IMPORTER_SKIP_POROSITY \
    -Dflys.backend.importer.skip.official.lines=$IMPORTER_SKIP_OFFICIAL_LINES \
    -Dflys.backend.importer.skip.prfs=$IMPORTER_SKIP_PRFS \
    -Dflys.backend.importer.skip.sediment.density=$IMPORTER_SKIP_SEDIMENT_DENSITY \
    -Dflys.backend.importer.skip.sediment.load=$IMPORTER_SKIP_SEDIMENT_LOAD \
    -Dflys.backend.importer.skip.sediment.load.ls=$IMPORTER_SKIP_SEDIMENT_LOAD_LS \
    -Dflys.backend.importer.skip.sq.relation=$IMPORTER_SKIP_SQ_RELATION \
    -Dflys.backend.importer.skip.w80s=$IMPORTER_SKIP_W80S \
    -Dflys.backend.importer.skip.w80.csvs=$IMPORTER_SKIP_W80_CSVS \
    -Dflys.backend.importer.skip.waterlevels=$IMPORTER_SKIP_WATERLEVELS \
    -Dflys.backend.importer.skip.waterlevel.differences=$IMPORTER_SKIP_WATERLEVEL_DIFFERENCES \
    -Dflys.backend.importer.skip.wst=$IMPORTER_SKIP_WST \
    -Dflys.backend.user=$BACKEND_USER \
    -Dflys.backend.password=$BACKEND_PASS \
    -Dflys.backend.url=$BACKEND_URL \
    -Dflys.backend.driver=$BACKEND_DB_DRIVER \
    -Dflys.backend.dialect=$BACKEND_DB_DIALECT \
     $JAR
