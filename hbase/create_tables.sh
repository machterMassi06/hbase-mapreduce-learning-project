#!/bin/bash

TABLE_PREFIX="web_site"

# initial table
COMMAND1="create '${TABLE_PREFIX}.visits', {NAME => 'info'}, SPLITS => ['DZ', 'ES', 'BE', 'FR', 'UK', 'USA']"

echo -e "$COMMAND1" | "$HBASE_HOME/bin/hbase" shell -n

#  stats Tables
COMMAND2="
create '${TABLE_PREFIX}.stats_country', {NAME => 'stats'}
create '${TABLE_PREFIX}.stats_user_id', {NAME => 'stats'}
create '${TABLE_PREFIX}.stats_page', {NAME => 'stats'}
"

echo -e "$COMMAND2" | "$HBASE_HOME/bin/hbase shell -n"