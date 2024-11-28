#!/bin/bash

mkdir -p datacagedb

DIR=`dirname $0`
DIR=`readlink -f "$DIR"`

if [ $# != 1 ]; then
    SCHEMA="$DIR/../doc/conf/datacage.sql"
else
    SCHEMA="$1"
fi

URL="jdbc:h2:`readlink -f datacagedb`/datacage"

mvn -e -Dexec.mainClass=org.h2.tools.RunScript exec:java \
    -Dexec.args="-url $URL -script $SCHEMA"
