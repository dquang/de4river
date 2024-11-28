#!/bin/bash
# Packaging-script for Dive4Elements River-Importer
#
# Copyright (C) 2011 - 2014 by Bundesanstalt für Gewässerkunde
# Software engineering by Intevation GmbH
#
# This file is Free Software under the GNU AGPL (>=v3)
# and comes with ABSOLUTELY NO WARRANTY! Check out the
# documentation coming with Dive4Elements River for details.

set -e

# See ../README for more information
# The working directory. Resulting tarball will be placed here.
PKG_DIR=/tmp/flys-importer
# Path to the flys checkout
SOURCE_DIR=$(readlink -f `dirname $0`)/../../..

usage(){
    cat << EOF

usage: $0 [options] VERSION [EXTRAS]

Create a D4E River Importer-package

OPTIONS:
   -?, --help          Show this message
   -o, --oracle        Package is for Oracle.
   VERSION must specify a tag (usually MAYOR.MINOR.PATCH) or a branch name.
   With EXTRAS, a tarball with dependencies can be given.
EOF
exit 0
}

OPTS=`getopt -o ?,o -l help,oracle -n $0 -- "$@"`

if [ $? != 0 ] ; then usage; fi
eval set -- "$OPTS"
while true ; do
  case "$1" in
    "-?"|"--help")
      usage;;
    "--")
      shift
      break;;
    "-o"|"--oracle")
      ORACLE="true"
      shift;;
    *)
      echo "Unknown Option $1"
      usage;;
  esac
done

if [ $# != 1 ]; then
    usage
fi

VERSION=$1
EXTRAS=$2

# Update to VERSION
echo "WARNING: any local changes in $SOURCE_DIR will be packaged."
cd ${SOURCE_DIR}
if [ -z "`hg tags | sed -n "/$VERSION/p"`" -a -z "`hg branches | sed -n "/$VERSION/p"`" ]
then
    echo "ERROR: No tag or branch $VERSION found in repository of $repo!"
    exit 1
else
    hg up "$VERSION"
fi

# create PDF of manual
REV=`hg parent | sed -n '1s/[[:alnum:]]*: *\([0-9]*:.*\)/\1/;1p'`
cd ${SOURCE_DIR}/backend/doc/documentation/de/
sed -i "s/documentrevision..rev.*/documentrevision}{rev$REV}/" \
    importer-manual.tex
# run pdflatex three times to get references and page numbering right
pdflatex importer-manual.tex
pdflatex importer-manual.tex
pdflatex importer-manual.tex

# package importer
# If ORACLE=true, the oracle profile in the POM is activated
mvn -f $SOURCE_DIR/backend/pom.xml clean package assembly:single

echo "INFO: create tarball"
rm -fr $PKG_DIR
mkdir $PKG_DIR
cd $PKG_DIR

mv $SOURCE_DIR/backend/target/river-backend-1.0-SNAPSHOT*-flys-importer.tar \
    flys-importer-$VERSION.tar
if [ -f "$EXTRAS" ]; then
    tar -xzf "$EXTRAS"
    tar -rf flys-importer-$VERSION.tar opt
fi

gzip flys-importer-$VERSION.tar
sha1sum flys-importer-$VERSION.tar.gz > flys-importer-$VERSION.tar.gz.sha1
echo Package is at: `readlink -f flys-importer-$VERSION.tar.gz`
