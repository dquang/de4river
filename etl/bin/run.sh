#!/bin/bash

APP=org.dive4elements.river.etl.aft.Sync

DIR=`dirname $0`
DIR=`readlink -f $DIR`

CONF=${1:-../doc/conf.xml}
CONF=$(readlink -f $CONF)
LOG4J_CONF=${2:-log4j2.xml}
LOG4J_CONF=$(readlink -f $LOG4J_CONF)

CLASSPATH=
for l in `find "$DIR/lib" -name \*.jar -print`; do
   CLASSPATH=$CLASSPATH:$l
done

export CLASSPATH

exec java \
    -Dlog4j2.configurationFile=file://$LOG4J_CONF \
    -Dconfig.file=$CONF \
    $APP
