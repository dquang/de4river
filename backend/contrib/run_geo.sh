#!/bin/bash

# Set this to your target database for Oracle
HOST=localhost
BACKEND_NAME="XE"
USER=flys28
PASS=flys28
# Alternatively you can provide a direct connection string:
# OGR_CONNECTION="PG:dbname=flys host=localhost port=5432 user=flys password=flys"

# Optional
VERBOSE=1
SKIP_AXIS=0
SKIP_KMS=0
SKIP_CROSSSECTIONS=0
SKIP_FIXPOINTS=0
SKIP_BUILDINGS=0
SKIP_FLOODPLAINS=0
SKIP_HYDR_BOUNDARIES=0
SKIP_HWS_LINES=0
SKIP_HWS_POINTS=0
SKIP_UESG=0
SKIP_DGM=0
SKIP_JETTIES=0
SKIP_FLOODMARKS=0

# Default encoding. Change here if necessary
export LC_ALL=de_DE@euro

# There should be no need to change anything below this line
GEW_FILE="$1"
RIVER_NAME=$(grep "Gew.sser" "$1" | sed 's/Gew.sser: //')

RIVER_PATH=$(grep "WSTDatei:" "$GEW_FILE" | awk '{print $2}')
RIVER_PATH=$(dirname "$RIVER_PATH")/../..
RIVER_PATH=$(readlink -f "$RIVER_PATH")

DIR=`dirname $0`
DIR=`readlink -f "$DIR"`

exec python $DIR/shpimporter/shpimporter.py \
    --directory $RIVER_PATH \
    --river_name "$RIVER_NAME" \
    --ogr_connection "$OGR_CONNECTION" \
    --host $HOST/$BACKEND_NAME \
    --user $USER \
    --password $PASS \
    --verbose $VERBOSE \
    --skip_axis $SKIP_AXIS \
    --skip_kms $SKIP_KMS \
    --skip_crosssections $SKIP_CROSSSECTIONS \
    --skip_fixpoints $SKIP_FIXPOINTS \
    --skip_buildings $SKIP_BUILDINGS \
    --skip_floodplains $SKIP_FLOODPLAINS \
    --skip_hydr_boundaries $SKIP_HYDR_BOUNDARIES \
    --skip_uesgs $SKIP_UESG \
    --skip_hws_lines $SKIP_HWS_LINES \
    --skip_hws_points $SKIP_HWS_POINTS \
    --skip_dgm $SKIP_DGM \
    --skip_jetties $SKIP_JETTIES \
    --skip_floodmarks $SKIP_FLOODMARKS \
    2>&1
