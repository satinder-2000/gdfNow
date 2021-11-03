@echo off
call mvn clean package
call docker build -t org.gdf/gdfNow .
call docker rm -f gdfNow
call docker run -d -p 9080:9080 -p 9443:9443 --name gdfNow org.gdf/gdfNow