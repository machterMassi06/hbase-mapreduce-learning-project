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
/workspace/run-spark.sh /workspace/read_Hbase_table.py <path_to_catalog>
```

### Example

For the `web_site.visits` table:

```bash
/workspace/run-spark.sh /workspace/read_Hbase_table.py /workspace/catalogs/visits.json
```

---

## 2. Spark–HBase Bulk Loading

The bulk loading process is implemented using a custom Java wrapper around HBase's `ThinBulkLoad` API.

The wrapper is responsible for generating HFiles from Spark DataFrames and loading them into HBase using the HBase bulk load mechanism.

The Java implementation can be found in:

```bash 
./mapreduce/src/main/java/mapreduce/hbase/ThinBulkLoad*.java
```

### Usage

1. Truncate the target HBase table to remove existing data (if required).
2. Execute the Spark bulk load job:

```bash
/workspace/run-spark.sh /workspace/bulk_load.py <path-to-catalog> <path-to-data-source> <hdfs-path-to-store-hfiles>
```

### Example: Bulk loading the `web_site.visits` table

First, truncate the existing HBase table:

```bash
hbase shell
```

```bash
hbase > truncate 'web_site.visits'
```

Then run the Spark job:

```bash
/workspace/run-spark.sh \
  /workspace/bulk_load.py \
  /workspace/catalogs/visits.json \
  hdfs://hadoop-hbase-cluster:9000/data/visits.csv \
  hdfs://hadoop-hbase-cluster:9000/hfiles_visits
```

---

## TO DO

- In `bulk_load.py`, evaluate the best strategy for rowkey ordering before HFile generation:
  - No explicit sorting
  - Global sorting using `orderBy("key")`
  - Controlling parallelism using `repartition(n, "key")`

Compare the impact of each approach on: HFile generation correctness, Number and size of generated HFiles, Execution time , Spark shuffle overhead, Scalability with larger datasets

