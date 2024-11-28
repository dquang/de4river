#!/bin/sh

# Use this on an exported query from roundup
# Known issue: Tiles with more then three , in them fail.
# Yes (ah) I do not know how to correctly handle the csv quoting...

echo "-------------"
echo "Unter anderem wurden folgende issues bearbeitet und können getestet werden:"

sed 's/"\(.*\),\(.*\)"/"\1§\2"/g' "$1" | \
sed 's/"\(.*\),\(.*\)"/"\1§\2"/g' | \
sed 's/"\(.*\),\(.*\)"/\1§\2/g' | \
gawk -F, '$5 > 5 {
print "- flys/issue"$2 " ("$1")"
print "[https://roundup-intern.intevation.de/flys/issue"$2"]"
print ""
}' | sed 's/§/,/g'


echo "-------------"
echo "Desweiteren gab es fortschritte in folgenden Issues:"
echo ""
sed 's/"\(.*\),\(.*\)"/"\1§\2"/g' "$1" | \
sed 's/"\(.*\),\(.*\)"/"\1§\2"/g' | \
sed 's/"\(.*\),\(.*\)"/\1§\2/g' | \
gawk -F, '$5 <= 5 && $5 > 0 {
print "- flys/issue"$2 " ("$1")"
print "[https://roundup-intern.intevation.de/flys/issue"$2"]"
print ""
}' | sed 's/§/,/g'

