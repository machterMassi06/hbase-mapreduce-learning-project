#!/bin/bash

set -e

if [ $# -lt 1 ]; then
    echo "Usage: $0 <python_file>"
    exit 1
fi

# Python scirpt and shift for parametres
PYTHON_FILE=$1
shift

LIB_DIR=/opt/spark/jars/hbase

PROJECT_JARS="$LIB_DIR/hbase-spark-hbase2.5.8_spark3.4.3_scala2.12.0_hadoop3.3.6.jar,\
$LIB_DIR/hbase-spark-protocol-shaded-hbase2.5.8_spark3.4.3_scala2.12.0_hadoop3.3.6.jar"

mkdir -p /tmp/.ivy2

echo "Launching Spark job..."

$SPARK_HOME/bin/spark-submit \
  --master spark://spark-master:7077 \
  --conf spark.jars.ivy=/tmp/.ivy2 \
  --packages \
org.slf4j:slf4j-api:1.7.36,\
org.slf4j:slf4j-simple:1.7.36,\
org.apache.hbase:hbase-shaded-client:2.5.8,\
org.apache.hbase:hbase-shaded-mapreduce:2.5.8 \
  --jars "$PROJECT_JARS" \
  "$PYTHON_FILE" "$@"