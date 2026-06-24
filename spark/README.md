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
