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

> ✅ Completed , code src in : ./mapreduce/src/main/java/mapreduce/nb_visits_by_column_TableMR

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

# Phase 5 – Data Enrichment Through Table Join

## New Requirement

The marketing department wants to enrich website visit records with user information in order to enable more advanced analytics.

First : generate data & create a new HBase table containing user profiles. 

---

## Objective

Perform a MapReduce join between:

```text
web_site.visits
web_site.users
```

and create a new enriched HBase table:

```text
web_site.visits_enriched
```

### new source table : web_site.users

```text 
RowKey=user_id: 0328

info:first_name = Alice
info:last_name = Martin
info:age = 28
info:gender = F
```

---

## Expected Output

### web_site.visits_enriched

```text 
RowKey: FR#0328#2025-05-21T08:54:58
```

```text
visit:user_id = 0328
visit:page = /home
visit:country = FR
visit:timestamp = 2025-05-21T08:54:58

user:first_name = Alice
user:last_name = Martin
user:age = 28
user:gender = F
```

---

## Join Key

The join must be performed using:

```text
user_id
```

---

# Phase 6 – Performance Evaluation

## New Requirement

As the volume of data grows, execution times become longer.

## Objective

Write a short analytical report / or Benchmark discussing:

### Advantages of MapReduce

* Scalability Hbase
* Distributed processing
* Bulk Load vs row by row PUTs vs Connector Spark

### Limitations of MapReduce

* High latency
* Disk-intensive processing
* Complexity of development

### Comparison with Modern Frameworks

* Apache Spark
