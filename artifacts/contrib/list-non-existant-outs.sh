#!/bin/bash

DIR=$(readlink -f `dirname $0`)

# write all outs expected in datacage to temporary file
tmpfile=`uuid`
cat $DIR/../doc/conf/meta-data.xml | \
    sed -n '/test="$out/p' | \
    sed "s/ *<dc:when test=\"\$out = '\(.*\)'\">/\1/" | \
    sort -u > /tmp/$tmpfile

echo "WARNING:"
echo "Only tests having exactly the pattern \"\$out = 'outname'\" will be recognised!"
echo
echo "outs not used in any artifact-configuration:"
for out in `cat /tmp/$tmpfile`
do
    count=`grep -l $out $DIR/../doc/conf/artifacts/*.xml | wc -l`
    if [ $count -lt 1 ]
    then
        echo " $out"
    fi
done

echo
echo "Caution: These might be set in gwt-client for inline datacage panels!"
echo
