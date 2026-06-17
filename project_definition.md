# Progressive Project Assignment: Web Traffic Analytics with HBase and MapReduce

## Context

A company operates a website that generates thousands of visits per day.

Navigation data is stored in CSV files. The company wants to build a Big Data platform based on Hadoop technologies in order to:

* Store navigation data efficiently.
* Perform large-scale analytical processing.
* Generate useful statistics for the marketing department.

You are responsible for developing a functional prototype using **HBase** and **MapReduce**.

The project requirements will evolve over time, simulating a real-world software development process.

---

# Phase 1 – Data Storage Design

## Objective

Design an HBase table capable of storing website visits.

Each visit contains the following fields:

| Field     |
| --------- |
| visit_id  |
| user_id   |
| page      |
| country   |
| timestamp |

## Deliverables

1. Create the HBase table (✅ Completed, see [`./hbase/create_tables.sh`](hbase/create_tables.sh) ) 
2. Justify the RowKey design : ✅ Completed

``` bash
The current RowKey design is: country#user_id#timestamp

Example:

FR#0328#2025-05-21T08:54:58

This design was chosen based on the expected analytical workloads of the project. Most future operations (ETL processes, analytical queries, aggregations, and joins) are expected to be performed primarily at the country level and then at the user level. Placing the country at the beginning of the RowKey allows related records to be grouped together and scanned efficiently.

The user_id component enables user-centric analysis, while the timestamp guarantees uniqueness and preserves chronological ordering.

In addition, the table was pre-split according to the six countries present in the dataset. Although the current environment runs in pseudo-distributed mode with a single RegionServer, this design anticipates future deployment on a larger cluster.

The RowKey design may evolve in future phases if new requirements emerge.
``` 
3. Insert at least 10000 records (✅ Completed, Data loading was performed using HBase Bulk Loading, which is the recommended approach for importing large datasets. Instead of inserting records individually through the HBase API, a MapReduce job was used to generate HFiles, which were then loaded directly into HBase regions. Source code: [`.mapreduce/src/main/java/mapreduce/bulk_load`](./mapreduce/src/main/java/mapreduce/bulk_load ) )
4. Verify the data using the HBase Shell (✅ Completed, The verification confirmed that all records were correctly loaded and accessible from the HBase table)


---

# Phase 2 – First MapReduce Job 

In 2 differents approch (Classic & Table mapred) :

> ✅ Completed , code src in : ./mapreduce/src/main/java/mapreduce/nb_visits_by_column_TableMR

> ✅ Completed , code src in : ./mapreduce/src/main/java/mapreduce/nb_visits_by_country


## New Requirement

The marketing team wants to know the number of visits per country.

## Objective

Develop a MapReduce job that reads data directly from HBase and computes:

| Country | Number of Visits |
| ------- | ---------------- |
| FR      | 520              |
| MA      | 180              |
| US      | 90               |

## Constraints

* Read data directly from HBase.
* Use `TableMapper`.
* Use `TableReducer`.

## Deliverables

* Mapper implementation.
* Reducer implementation.
* Execution results.

---

# Phase 3 – Client Feedback

> ✅ Completed , code src in : ./mapreduce/src/main/java/mapreduce/nb_visits_by_column_TableMR


## New Requirement

The client now wants to identify the most active users.

## Objective

Develop a second MapReduce job that computes:

```text
user_id -> total number of visits
```

## Expected Output

| User ID | Visit Count |
| ------- | ----------- |
| U001    | 128         |
| U057    | 103         |
| U132    | 95          |

## Bonus

Generate a Top-10 Most Active Users report.

---

# Phase 4 – Industrialization of TableMR to persiste data in Hbase tables 

## New Requirement

The company wants to persist analytical results.

## Objective

Create a new HBase table:

```text
stats_users, stats_country, stat_pages 
```

Store the output of the MapReduce job directly in HBase.

### Example (stats_country)

```text
RowKey: FR

stats:visit_count = 1619 
```

## Deliverables

* Table creation in java.
* MapReduce integration with HBase output.
* Verification of stored statistics.
---

# Phase 5 – Advanced Analytics

## New Requirement

The marketing department asks:

> Which pages are the most visited in each country?

### Example

```text
FR -> /home -> 5400
FR -> /products -> 2100

US -> /home -> 1200
US -> /pricing -> 950
```

## Objective

Design a MapReduce job capable of producing these statistics.

### Hint

A composite intermediate key may be useful:

```text
(country, page)
```

## Deliverables

* Mapper design.
* Reducer design.
* Final statistics.

---

# Phase 6 – Performance Evaluation

## New Requirement

As the volume of data grows, execution times become longer.

## Objective

Write a short analytical report discussing:

### Advantages of MapReduce

* Scalability
* Fault tolerance
* Distributed processing

### Limitations of MapReduce

* High latency
* Disk-intensive processing
* Complexity of development

### Comparison with Modern Frameworks

* Apache Spark
* Apache Flink

---

# Phase 7 – Final Presentation

Prepare a final presentation and be ready to answer the following questions.

## Oral Examination Questions

1. Why did you choose HBase?
2. Why is RowKey design important?
3. How does HBase distribute data across RegionServers?
4. What is the difference between HDFS and HBase?
5. Why use MapReduce on top of HBase?
6. What happens when a RegionServer fails?
7. Why is Apache Spark generally faster than MapReduce?
8. Which analytics could be performed in real time using Flink?

---

# Expected Architecture

```text
CSV Dataset
     |
     v
+-----------+
|   HBase   |
+-----------+
     |
     v
+-----------+
| MapReduce |
+-----------+
     |
     v
+-----------+
| Statistics|
|   HBase   |
+-----------+
```

---

# Project Goal

The purpose of this project is to demonstrate practical knowledge of:

* Hadoop Ecosystem
* HDFS
* HBase
* MapReduce Programming
* Distributed Data Processing
* Big Data System Design
* Performance Evaluation

By the end of the project, students should be able to design, implement, and evaluate a complete analytical pipeline using HBase and MapReduce.
