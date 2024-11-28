#!/bin/bash
# Release script for Dive4Elements River
#
# Authors:
# Andre Heinecke <aheinecke@intevation.de>
# Tom Gottfried <tom@intevation.de>
#
# Copyright (C) 2011 - 2014, 2020 by Bundesanstalt für Gewässerkunde
# Software engineering by Intevation GmbH
#
# This file is Free Software under the GNU AGPL (>=v3)
# and comes with ABSOLUTELY NO WARRANTY! Check out the
# documentation coming with Dive4Elements River for details.

set -e
LC_ALL=en_US.UTF-8
DEFAULT_WD=/tmp/flys-release

ARTIFACTS_HG_REPO="https://wald.intevation.org/hg/dive4elements/framework"
HTTPCLIIENT_HG_REPO="https://wald.intevation.org/hg/dive4elements/http-client"
FLYS_HG_REPO="https://wald.intevation.org/hg/dive4elements/river"

REPOS="river http-client framework"

SCRIPT_DIR=$(readlink -f `dirname $0`)
usage(){
    cat << EOF

usage: $0 [options] TARGET

Create a D4E River package

OPTIONS:
   -?, --help          Show this message
   -w                  The working directory to use (do not use spaces in path)
                       Default: $DEFAULT_WD
   -t                  Tag the selected branch with given name.
                       Note that \$USER is used as the repository user name
                       for pushing the tags.
   -o, --oracle        Release is for oracle.
   TARGET must specify a tag (usually MAYOR.MINOR.PATCH) or a branch name.
EOF
exit 0
}
#   --backend-db-url                Url of database backend. Default: $BACKENDURL
#   --backend-db-pass               Backend db password. Default: $BACKENDPASS
#   --backend-db-port               Backend db port. Default: $BACKENDPORT
#   --backend-db-user               Backend db user. Default: $BACKENDUSER
#   --backend-db-backend            Backend db backend name. Default: $BACKENDBACK
#   --seddb-url                     Sediment db url. Default: $SEDDBURL
#   --seddb-port                    Sediment db port. Default: $SEDDBPORT
#   --seddb-user                    Sediment db user. Default: $SEDDBUSER
#   --seddb-pass                    Sediment db password. Default: $SEDDBPASS
#   --seddb-back                    Sediment db backend. Default: $SEDDBBACK

# customizable variables
ELBE_MODEL_UUID=${ELBE_MODEL_UUID:-1a4825f6-925f-11e3-8165-001f29e71d12}

OPTS=`getopt -o ?w:,?t:,o \
     -l help,oracle \
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
    "-w")
      WORK_DIR=$2
      shift 2;;
    "-o"|"--oracle")
      ORACLE="true"
      shift;;
    "-t")
      DO_TAG=$2
      shift 2;;
    *)
      echo "Unknown Option $1"
      usage;;
  esac
done

if [ $# != 1 ]; then
    usage
fi

TARGET=$1

if [ -z $WORK_DIR ]; then
  WORK_DIR=$DEFAULT_WD
fi

mkdir -p $WORK_DIR

if [ -z "$SMARTGWT" ]; then
    echo "ERROR: Variable SMARTGWT not set."
    echo "Please set this variable to point to the smartgwt .jar archive to be used."
    exit 1
fi

if [ ! -f "$SMARTGWT" ]; then
    echo "ERROR: Failed to find smartgwt archive at: '$SMARTGWT'"
    exit 1
fi

if [ ! -d "$FLYS_SOURCE_DIR" ]; then
    mkdir -p ${FLYS_SOURCE_DIR:=$(mktemp -d)}
    echo "Will use source code checkouts in $FLYS_SOURCE_DIR"
    echo "Cloning sources"
    cd $FLYS_SOURCE_DIR
    hg clone $ARTIFACTS_HG_REPO framework
    hg clone $HTTPCLIIENT_HG_REPO http-client
    hg clone $FLYS_HG_REPO river
else
    echo "Updating sources / Reverting changes"
    cd $FLYS_SOURCE_DIR
    for repo in $REPOS; do
        cd $repo && hg revert -a && hg pull -u && cd -
    done;
fi

# Update to current version
for repo in $REPOS; do
    cd $repo
    if [ -z "`hg tags | sed -n "/$TARGET/p"`" -a -z "`hg branches | sed -n "/$TARGET/p"`" ]
    then
        echo "WARNING: No tag or branch $TARGET found in repository of $repo!"
        echo "         Will fallback to 'default'!"
        hg up default
    else
        hg up "$TARGET"
    fi
    cd $FLYS_SOURCE_DIR
done

if [ -n "$DO_TAG" ]; then
    VERSION=$DO_TAG
    echo "INFO: Tagging current branch as $VERSION"
    for repo in $REPOS; do
        cd $repo
        CHANGESET=$(hg parent |head -1 | awk -F: '{print $3}')
        echo ""
        echo "Do you really want to tag $repo rev: $CHANGESET as Version $VERSION?"
        echo "press enter to continue or CTRL+C to abort."
        echo ""
        hg log -r $CHANGESET -l1
        read
        OLD_REV=$(cat .hgtags | tail -1 | awk '{print $2}')
        hg tag -r $CHANGESET -m "Added tag $VERSION for changeset $CHANGESET" \
           "$VERSION"
        hg push ssh://$USER@scm.wald.intevation.org/hg/dive4elements/$repo
        echo "Changelog for $repo" >> $WORK_DIR/changes_$OLD_REV-$VERSION.txt
        echo "#############################################################################" \
            >> $WORK_DIR/changes_$OLD_REV-$VERSION.txt
        hg log -r $VERSION:$OLD_REV --style changelog >> $WORK_DIR/changes_$OLD_REV-$VERSION.txt
        cd $FLYS_SOURCE_DIR
    done;
else
    VERSION=$TARGET
fi

if [ ! -f "$FLYS_SOURCE_DIR/OpenLayers-2.11.tar.gz" ]; then
    echo "INFO: download OpenLayers-2.11 for client"
    cd $FLYS_SOURCE_DIR
    curl -LO "https://github.com/openlayers/ol2/releases/download/release-2.11/OpenLayers-2.11.tar.gz"
    tar xvfz OpenLayers-2.11.tar.gz
    # TODO: Remove more superfluous OpenLayers stuff.
    rm -rf OpenLayers-2.11/doc
    rm -rf OpenLayers-2.11/tests
    rm -rf OpenLayers-2.11/examples
    cd $WORK_DIR
fi
cp -r $FLYS_SOURCE_DIR/OpenLayers-2.11 \
    $FLYS_SOURCE_DIR/river/gwt-client/src/main/webapp/


echo "INFO: Installing smartGWT"

mvn install:install-file -Dfile="$SMARTGWT" -Dversion=4.1-p20141119 \
    -DartifactId=smartgwt-lgpl -DgroupId=com.isomorphic.smartgwt.lgpl \
    -Dpackaging=jar

echo "INFO: compile sources"
mvn -f $FLYS_SOURCE_DIR/framework/pom.xml clean compile install

# If ORACLE=true, the oracle profile in the POM is activated
mvn -f $FLYS_SOURCE_DIR/river/backend/pom.xml clean compile install

mvn -f $FLYS_SOURCE_DIR/river/artifacts/pom.xml clean compile
mvn -f $FLYS_SOURCE_DIR/http-client/pom.xml clean compile install
# gwt-client has to be compiled later to allow custom CLIENT_CONF

echo "INFO: create h2 database for artifacts and datacage"
cd $FLYS_SOURCE_DIR/river/artifacts
rm -rf artifactsdb datacagedb
$FLYS_SOURCE_DIR/framework/artifact-database/bin/createArtifacts.sh
$FLYS_SOURCE_DIR/river/artifacts/bin/createDatacage.sh
cd -

echo "INFO: download WSPLGEN"
cd $WORK_DIR
rm -f wsplgen-linux-*bit-static
wget \
    https://wald.intevation.org/frs/download.php/1496/wsplgen-linux-64bit-static.gz \
    https://wald.intevation.org/frs/download.php/1498/wsplgen-linux-32bit-static.gz
gunzip wsplgen-linux-64bit-static.gz wsplgen-linux-32bit-static.gz
chmod +x wsplgen-linux-*
cp wsplgen-linux-32bit-static $FLYS_SOURCE_DIR/river/artifacts/bin/wsplgen
cd -

if [ -n "$DO_TAG" ]; then
    echo "INFO: Building packages for publication"
    cd $FLYS_SOURCE_DIR/river/artifacts
    mvn package assembly:single
    mv target/river-artifacts-1.0-SNAPSHOT-bin.tar.bz2 \
        $WORK_DIR/d4e-river-$VERSION.tar.bz2 && cd ..
    cd gwt-client && mvn clean compile package
    mv target/gwt-client-1.0-SNAPSHOT.war \
        $WORK_DIR/d4e-river-$VERSION.war && cd ..
fi


echo "INFO: Preparing configuration of web client"

#Needs to be done before the tomcat replacement below
sed -i -e "s@1a4825f6-925f-11e3-8165-001f29e71d12@${ELBE_MODEL_UUID}@g" \
    $FLYS_SOURCE_DIR/river/gwt-client/src/main/webapp/images/FLYS_Karte_interactive.html

if [ -d "$WEBINF" ]; then
    echo "INFO: copy custom client configuration to target destination"
    WEBINF_DIR=$FLYS_SOURCE_DIR/river/gwt-client/src/main/webapp/WEB-INF/
    cp -R $WEBINF/* $WEBINF_DIR
    for file in `find $WEBINF_DIR/ -type f`; do
        sed -i -e "s@D4E_VERSION@${VERSION}@g" $file
    done
fi

if [ -d "$CLIENT_RESOURCES" ]; then
    echo "INFO: copy custom client resources to target destination"
    CLIENT_RESOURCES_DIR=$FLYS_SOURCE_DIR/river/gwt-client/src/main/resources/
    cp -R $CLIENT_RESOURCES/* $CLIENT_RESOURCES_DIR
    for file in `find $CLIENT_RESOURCES_DIR/ -type f`; do
        sed -i -e "s@D4E_VERSION@${VERSION}@g" $file
    done
fi

if [ -f "$CLIENT_CONF" ]; then
    echo "INFO: copy custom java script client configuration to target destination"
    cp "$CLIENT_CONF" \
        $FLYS_SOURCE_DIR/river/gwt-client/src/main/java/org/dive4elements/river/client/client/config.xml
fi

if [ -n "$DGM_PATH" ]; then
    sed -i -e "s@<dgm-path>.*</dgm-path>@<dgm-path>${DGM_PATH}</dgm-path>@g" \
        $FLYS_SOURCE_DIR/river/artifacts/doc/conf/conf.xml
fi

if [ -n "$WIKI_URL" ]; then
    sed -i -e "s@<help-url>http://example.com</help-url>@<help-url>${WIKI_URL}</help-url>@g" \
        $FLYS_SOURCE_DIR/river/artifacts/doc/conf/conf.xml
fi

if [ -d "$SERVER_CONF" ]; then
    echo "INFO: copy custom server configuration to target destination"
    cp -R $SERVER_CONF/* $FLYS_SOURCE_DIR/river/artifacts/doc/conf/
    for file in `find $FLYS_SOURCE_DIR/river/artifacts/doc/conf -type f`; do
        sed -i -e "s@D4E_VERSION@${VERSION}@g" $file
    done
fi


echo "INFO: compile and build sources"
cp $WORK_DIR/wsplgen-linux-64bit-static \
    $FLYS_SOURCE_DIR/river/artifacts/bin/wsplgen

mvn -f $FLYS_SOURCE_DIR/river/artifacts/pom.xml package assembly:single
mvn -f $FLYS_SOURCE_DIR/river/gwt-client/pom.xml clean compile package


echo "INFO: create tarball"
cd $WORK_DIR
mv $FLYS_SOURCE_DIR/river/artifacts/target/river-artifacts-1.0-SNAPSHOT-bin.tar \
    flys-$VERSION.tar
mv $FLYS_SOURCE_DIR/river/gwt-client/target/gwt-client-1.0-SNAPSHOT.war \
    flys-${VERSION}.war
tar -rf flys-$VERSION.tar flys-${VERSION}.war

if [ -f "$INSTALL" ]; then
    echo "INFO: prepare custom installation script"
    cp $INSTALL .
    sed -i "s/D4E_VERSION/$VERSION/g" ./`basename $INSTALL`
    tar -rf flys-$VERSION.tar `basename $INSTALL`
fi

## TODO: tag with maven and include basedir with correct name in assembly ##
rm -rf flys-$VERSION && mkdir flys-$VERSION
tar -xf flys-$VERSION.tar -C flys-$VERSION
tar cfz flys-$VERSION.tar.gz flys-$VERSION
##

if [ -n "$DO_TAG" ]; then
    echo "Binary-packages for publication:"
    echo "_ $WORK_DIR/d4e-river-$VERSION.tar.bz2"
    echo "_ $WORK_DIR/d4e-river-$VERSION.war"
    echo "Changelog: $WORK_DIR/changes_$OLD_REV-$VERSION.txt"
fi
echo "DONE: $WORK_DIR/flys-$VERSION.tar.gz"
