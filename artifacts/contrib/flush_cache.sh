#!/bin/bash

if [ $# != 2 ]; then
    echo "usage $0 <artifact-server-port> <cachename>"
    echo "    cachename is the name of the cache defined in conf/caches.xml"
    echo "    for example: $0 12123 datacage.db"
    exit 1
fi

curl -d @- http://localhost:$1/service/cache-invalidation << EOF 
<?xml version="1.0" ?>
<caches>
  <cache name="$2"/>
</caches>
EOF
