#!/bin/bash -

printf "DB username : " ; read -r username

mysql < ~/Documents/L2/discool/src/main/resources/rebuild_db_no_cred.sql -u $username -p
