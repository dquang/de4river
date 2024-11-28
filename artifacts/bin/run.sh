#!/bin/bash

APP=org.dive4elements.artifactdatabase.App

DIR=`dirname $0`/..
DIR=`readlink -f "$DIR"`

CLASSPATH=
for l in `find "$DIR/bin/lib" -name \*.jar -print`; do
   CLASSPATH=$CLASSPATH:$l
done

export CLASSPATH

exec java $JAVA_OPTS -Xmx1024m \
     -server \
     -Djava.awt.headless=true \
     -Dflys.datacage.recommendations.development=false \
     -Djava.io.tmpdir="$DIR/cache" \
     -Dflys.uesk.keep.artifactsdir=false \
     -Dwsplgen.bin.path="$DIR/bin/wsplgen" \
     -Dwsplgen.log.output=false \
     -Dartifact.database.dir="$DIR/conf" \
     -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager \
     -Dlog4j2.configurationFile="file://$DIR/conf/log4j2.xml" \
     $APP
