# HBase Benchmarking and Performance Evaluation

This document summarizes the main approaches, tools, and metrics that can be used to benchmark, evaluate, and monitor an Apache HBase cluster, whether running in pseudo-distributed mode or on a **real distributed cluster**.

---

# Benchmarking Objectives

Benchmarking an HBase cluster helps answer questions such as:

- What throughput can the cluster sustain? What are the read and write latencies?
- How does performance evolve as the dataset grows? How does the system behave under concurrent workloads?
- What are the main bottlenecks (CPU, memory, network, disk, RegionServers)?
- How does the row key design impact performance? What is the effect of region splits ?

---

# 1. Native HBase Benchmarking Tool: `PerformanceEvaluation`

## Overview

`PerformanceEvaluation` is the benchmark utility included with HBase.

It allows testing the performance of common HBase operations directly against a table.

Official documentation:

https://hbase.apache.org/2.3/testdevapidocs/org/apache/hadoop/hbase/PerformanceEvaluation.html

The tool can run either:

- As a MapReduce job (distributed benchmark)
- In standalone mode (`--nomapred`)

Typical operations include:

| Test | Description |
|--------|-------------|
| `randomRead` | Random row reads |
| `randomWrite` | Random row writes |
| `sequentialRead` | Sequential row reads |
| `sequentialWrite` | Sequential row writes |
| `scan` | Full table scans |
| `randomSeekScan` | Random scans |
| `filteredScan` | Scan with filters |

---

## `PerformanceEvaluation` Setup

Before running benchmarks, ensure: HBase, Hadoop/YARN (for MapReduce Mode) is running, and the target table exits. 

Example:

```bash
hbase shell
```

```ruby
count 'web_site.visits'
```

```text
10000 row(s)
```

---

## Two ways to run the tool

### 1. Running a Distributed Benchmark (MapReduce Mode)

The following command launches a distributed benchmark using MapReduce.

Example : Operation = random reads

```bash
hbase org.apache.hadoop.hbase.PerformanceEvaluation \
  --table=web_site.visits \
  randomRead 5
```

Meaning: `randomRead` = benchmark type, here Random row reads and `5` = number of clients = nb mapper tasks

PerformanceEvaluation will: Generate input splits, launch a MapReduce job, create one mapper per client (each mapper performs random reads independently) and aggregate benchmark results.


### 2. Running Without MapReduce

For quick tests on a single machine (standalone & psuedo distributed cluster):

```bash
hbase org.apache.hadoop.hbase.PerformanceEvaluation \
  --nomapred \
  --table=web_site.visits \
  randomRead 5
```

This executes locally without creating a MapReduce job. Useful for: Functional testing, small-scale experiments and development environments

---
## Examples of operations using the `PerformanceEvaluation` tool

### Random Read Benchmark

```bash
hbase org.apache.hadoop.hbase.PerformanceEvaluation \
  --table=web_site.visits \
  randomRead 5
```

### Random Write Benchmark

```bash
hbase org.apache.hadoop.hbase.PerformanceEvaluation \
  --table=web_site.visits \
  randomWrite 5
```

Measures: Write scalability, Region distribution and MemStore pressure

### Sequential Read Benchmark

```bash
hbase org.apache.hadoop.hbase.PerformanceEvaluation \
  --table=web_site.visits \
  sequentialRead 5
```

Measures: Scan efficiency, Region locality performance and Block cache effectiveness


### Sequential Write Benchmark

```bash
hbase org.apache.hadoop.hbase.PerformanceEvaluation \
  --table=web_site.visits \
  sequentialWrite 5
```

Measures: WAL performance;MemStore throughput and Flush efficiency


### Scan Benchmark

```bash
hbase org.apache.hadoop.hbase.PerformanceEvaluation \
  --table=web_site.visits \
  scan 5
```

Measures: Full scan throughput, RegionServer scan performance and Block cache utilization

---

## Important Parameters

### Number of Clients

> `OPS 10` , Creates: 10 parallel benchmark clients
> In MapReduce mode this typically results in 10 mapper tasks

---

### Disable MapReduce

> `--nomapred` : for Runs locally

---

### Target Table
> `--table=web_site.visits` : Specifies which table is benchmarked

---

## Metrics Collected

PerformanceEvaluation reports: Throughput (ops/sec), Execution time, Number of operations, Average latency and Resource consumption

Example output:

```text
Finished 100000 operations

Throughput = 25000 ops/sec
Average latency = 2.1 ms
...
```

---

## Advantages vs Limitations

| Advantages | Limitations |
|------------|-------------|
| Included with HBase | Limited workload customization |
| Very easy to use | Limited reporting capabilities |
| No external dependencies | Not representative of complex application workloads |
| Useful for quick performance validation | Less flexible than YCSB |

---

# 2. YCSB (Yahoo! Cloud Serving Benchmark)

## Overview

YCSB (Yahoo! Cloud Serving Benchmark) is the de facto standard benchmarking framework for NoSQL databases.

It supports HBase, Cassandra, MongoDB, Redis and many other systems

YCSB is particularly useful because it reproduces realistic application workloads (A, B, C, D, E, F).

Official repository: https://github.com/brianfrankcooper/YCSB

## YCSB Setup

### Clone the Project

```bash
git clone https://github.com/brianfrankcooper/YCSB.git
```

### Build YCSB

```bash
cd YCSB
mvn -pl site.ycsb:hbase20-binding -am clean package
```

### Verify Build

```bash
ls bin/ycsb
```

Expected: `bin/ycsb`

---
## HBase Configuration

Ensure: HBase and ZooKeper is running, and `export HBASE_CONF_DIR=$HBASE_HOME/conf`

## Creating a Test Table in HBase

Example: hbase > `create 'ycsb_table', 'family'` 

## Loading Initial Data

Before executing a benchmark, YCSB typically loads an initial dataset into an HBase table.

Example:

```bash 
bin/ycsb load hbase20 \
  -P workloads/workloada \
  -p table=ycsb_table \
  -p columnfamily=family \
  -p recordcount=100000
``` 

This command inserts 100,000 records into the `ycsb_table` table. **If no table is explicitly specified**, YCSB uses the default table configured in its properties (commonly `usertable`).

--- 
## Running a Benchmark (Standard YCSB Workloads)

| Workload | Description             | Typical Use Case                  | Command (example)                                                                                  |
| -------- | ----------------------- | --------------------------------- | ----------------------------------------------------------------------------------------- |
| **A**    | 50% reads / 50% updates | Balanced read/write workload      | `bin/ycsb run hbase20 -P workloads/workloada -p table=ycsb_table -p operationcount=10000` |
| **B**    | 95% reads / 5% updates  | Read-heavy applications           | `bin/ycsb run hbase20 -P workloads/workloadb -p table=ycsb_table -p operationcount=10000` |
| **C**    | 100% reads              | Cache-like workloads              | `bin/ycsb run hbase20 -P workloads/workloadc -p table=ycsb_table -p operationcount=10000` |
| **D**    | Read latest records     | Time-series applications          | `bin/ycsb run hbase20 -P workloads/workloadd -p table=ycsb_table -p operationcount=10000` |
| **E**    | Short range scans       | Analytics and reporting workloads | `bin/ycsb run hbase20 -P workloads/workloade -p table=ycsb_table -p operationcount=10000` |
| **F**    | Read-modify-write       | Transactional workloads           | `bin/ycsb run hbase20 -P workloads/workloadf -p table=ycsb_table -p operationcount=10000` |


## Useful YCSB Parameters

| Parameter                          | Example                          | Description                                               |
| ---------------------------------- | -------------------------------- | --------------------------------------------------------- |
| **Record count**                   | `-p recordcount=1000000`         | Dataset size used for the benchmark                       |
| **Operation count**                | `-p operationcount=500000`       | Total number of operations executed                       |
| **Threads (N)**                    | `-threads <N>`                     | Number of concurrent client threads (controls load level) |
| **Request distribution (uniform)** | `-p requestdistribution=uniform` | Even access across all records                            |
| **Request distribution (zipfian)** | `-p requestdistribution=zipfian` | Skewed access pattern (realistic workloads)               |

---
## Metrics Collected by YCSB

YCSB provides detailed performance metrics: 

```text
Throughput (ops/sec)
Average latency
Min latency
Max latency
95th percentile latency
99th percentile latency
...
```
---

## Advantages and Limitations of YCSB 

| Advantages                        | Limitations                                    |
| --------------------------------- | ---------------------------------------------- |
| Industry standard                 | Additional setup required                      |
| Highly configurable               | More complex than PerformanceEvaluation tool   |
| Supports realistic workloads      | Requires workload tuning for realistic results |
| Detailed latency statistics       | —                                              |
| Easy comparison between databases | —                                              |


