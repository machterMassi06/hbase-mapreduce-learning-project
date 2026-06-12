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

1. Create the HBase table.
2. Justify the RowKey design.
3. Insert at least 1,000 records.
4. Verify the data using the HBase Shell.

## Questions

* Why did you choose this RowKey?
* What are the risks of a poor RowKey design?
* Why is HBase suitable for this use case?

---

# Phase 2 – First MapReduce Job

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

# Phase 4 – Industrialization

## New Requirement

The company wants to persist analytical results.

## Objective

Create a new HBase table:

```text
stats_users
```

Store the output of the MapReduce job directly in HBase.

### Example

```text
RowKey: U001

stats:visit_count = 128
```

## Deliverables

* Table creation script.
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

# Evaluation Criteria

| Criterion                | Weight |
| ------------------------ | ------ |
| HBase Data Modeling      | 20%    |
| Data Loading             | 10%    |
| MapReduce Implementation | 30%    |
| Result Quality           | 15%    |
| Performance Analysis     | 15%    |
| Final Presentation       | 10%    |

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
