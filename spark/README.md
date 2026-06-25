# HBase–Spark Connector

In this section, I explore the **HBase–Spark connector** using Python. Apache Spark is a distributed computing framework and the successor to MapReduce. The objective is to implement several use cases enabled by this connector, including reading and loading HBase tables, simple ingestion workflows, and bulk loading operations.

## 0. Setup

First, you need to enter the Spark master container:

```bash
sudo docker exec -it spark-master bash
```

The Spark project is mounted inside the container under `/workspace/*`.

The script `workspace/run-spark.sh` automatically handles Spark job execution. It wraps the `spark-submit` command with the required options (such as `--packages`, `--jars`, etc.), so you don’t need to specify them manually.

In general, it is used as follows:

```bash
/workspace/run-spark.sh <python-file> [extra parameters for the Python script]
```

This script simplifies and automates the execution of Spark jobs.

## 1. Reading an HBase table

Python script: `spark/read_Hbase_table.py`

### Usage

You must provide a **catalog file**, which defines the schema (i.e., the representation of the HBase table you want to read). Note that the table must already exist in HBase.

```bash
/workspace/run-spark.sh read_Hbase_table.py <path_to_catalog>
```

### Example

For the `web_site.visits` table:

```bash
/workspace/run-spark.sh read_Hbase_table.py catalogs/visits.json
```

---

## 2. Spark–HBase Bulk Loading

> > See TODO section to fix the current issue 

### Usage

1. Truncate the HBase table (to remove existing data)
2. Run the Spark job:

```bash
/workspace/run-spark.sh bulk_load.py <path-to-catalog> <path-to-data-source> <tmp-path-to-store-hfiles>
```

### Example (for `web_site.visits` table)

```bash
hbase shell
hbase > truncate 'web_site.visits'
```

Then run:

```bash
/workspace/run-spark.sh bulk_load.py catalogs/visits.json hdfs://hadoop-hbase-cluster:9000/data/visits.csv /tmp/hfiles_visits
```

---

## TODO

**bulk load** : Fix the following issue:

```
Traceback (most recent call last):
  File "/workspace/bulk_load.py", line 194, in <module>
    thin_bulk_load(sys.argv[1], sys.argv[2], sys.argv[3])
  File "/workspace/bulk_load.py", line 138, in thin_bulk_load
    hbase_context.hbaseBulkLoadThinRows(
  File "/opt/spark/python/lib/py4j-0.10.9.7-src.zip/py4j/java_gateway.py", line 1314, in __call__
  File "/opt/spark/python/lib/py4j-0.10.9.7-src.zip/py4j/java_gateway.py", line 1283, in _build_args
  File "/opt/spark/python/lib/py4j-0.10.9.7-src.zip/py4j/java_gateway.py", line 1283, in <listcomp>
  File "/opt/spark/python/lib/py4j/py4j/protocol.py", line 298, in get_command_part
AttributeError: 'function' object has no attribute '_get_object_id'
```

**Root cause**

The Python lambda function:

```python
lambda t: (t[0], t[1])
```

cannot be converted by Py4J into a Scala/Java function, causing a serialization failure between Python and the JVM.

