#!/bin/bash

# $1: name, user and password for new DB (optional. Default: d4e)
# $2: host (optional. Default: localhost)

SCRIPT_DIR=`dirname $0`
DB_NAME=${1:-d4e}
PG_HOST=${2:-localhost}

# run as user postgres (postgresql super-user)
# it is assumed that the owner of the DB has the same name as the DB!

# create PostGIS-DB
createuser -S -D -R $DB_NAME
createdb $DB_NAME

psql -d $DB_NAME -c "ALTER USER $DB_NAME WITH PASSWORD '$DB_NAME';"

psql -d $DB_NAME -c "CREATE EXTENSION postgis;"
psql -d $DB_NAME -c "GRANT ALL ON geometry_columns TO $DB_NAME;
                     GRANT ALL ON geography_columns TO $DB_NAME;
                     GRANT ALL ON spatial_ref_sys TO $DB_NAME;"

# add credentials to .pgpass (or create .pgpass)
echo "*:*:$DB_NAME:$DB_NAME:$DB_NAME" >> ~/.pgpass
chmod 0600 ~/.pgpass

# apply schema-scripts
psql -d $DB_NAME -U $DB_NAME -h $PG_HOST -f $SCRIPT_DIR/postgresql.sql
psql -d $DB_NAME -U $DB_NAME -h $PG_HOST -f $SCRIPT_DIR/postgresql-spatial.sql
psql -d $DB_NAME -U $DB_NAME -h $PG_HOST -f $SCRIPT_DIR/postgresql-minfo.sql
