#!/bin/bash -

printf "DB username : " ; read -r username

mysql < src/main/resources/build_db.sql -u $username -p

&& mvn install
