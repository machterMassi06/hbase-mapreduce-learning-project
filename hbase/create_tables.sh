#!/bin/bash

usage() {
  echo "Usage: $0 <HBASE_HOME>"
  exit 1
}

# Check parameter
if [ -z "$1" ]; then
  usage
fi

HBASE_HOME="$1"
TABLE_PREFIX="web_site"

# Maybe in future: salting & pre-splitting (for real distributed HBase cluster)

COMMAND="create '${TABLE_PREFIX}.visits', {NAME => 'info'}, SPLITS => ['DZ', 'ES', 'BE', 'FR', 'UK', 'USA']"

echo -e "$COMMAND" | "$HBASE_HOME/bin/hbase" shell -n