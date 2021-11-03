#!/bin/sh
mvn clean package && docker build -t org.gdf/gdfNow .
docker rm -f gdfNow || true && docker run -d -p 9080:9080 -p 9443:9443 --name gdfNow org.gdf/gdfNow