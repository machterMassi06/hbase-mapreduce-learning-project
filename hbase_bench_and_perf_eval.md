# HBase Benchmarking and Performance Evaluation

This document summarizes the main approaches, tools, and metrics that can be used to benchmark, evaluate, and monitor an Apache HBase cluster, whether running in pseudo-distributed mode or on a **real distributed cluster**.

---

# 1. Benchmarking Objectives

Benchmarking an HBase cluster helps answer questions such as:

- What throughput can the cluster sustain? What are the read and write latencies?
- How does performance evolve as the dataset grows? How does the system behave under concurrent workloads?
- What are the main bottlenecks (CPU, memory, network, disk, RegionServers)?
- How does the row key design impact performance? What is the effect of region splits ?

---

# 2. Native HBase Benchmarking Tool: `PerformanceEvaluation`

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


### 2 Running Without MapReduce

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
> --table=web_site.visits` : Specifies which table is benchmarked

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
