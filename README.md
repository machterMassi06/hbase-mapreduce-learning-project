# HBase & MapReduce project - Usage Cases

The motivation behind this repo is to share my learning (2025/2026) in Big Data technologies, including:

- HBase (distributed database)
- HDFS (Hadoop Distributed File System)
- MapReduce

This repository is mainly experimental (R&D) and educational.

In [`./project_definition.md`](./project_definition.md), you will find a simple project definition that serves as a roadmap.  
It may evolve over time as the project grows :)

---

# 1 - Setup

## Clone the repository

```bash
git clone https://github.com/machterMassi06/hbase-mapreduce-learning-project
cd hbase_mapreduce_project
```

---

## Start a ready-to-use Hadoop + HBase pseudo distr cluster 

To avoid setup, you can use [my Docker-based cluster repo](https://github.com/machterMassi06/Hbase-pseudo-distributed) (pseudo-distributed mode) that includes HDFS,YARN, HBase. You can pull the docker image into your locale machine with the following command : 

```bash 
 docker pull massmach/hadoop-hbase-cluster:latest
```

---

## Run the container

Once the cluster image is built, start a container and mount the current project directory into `/workspace`:

```bash
docker run -d \
  -p 9870:9870 \
  -p 8088:8088 \
  -p 16010:16010 \
  -v "$PWD:/workspace" \
  massmach/hadoop-hbase-cluster:latest

```

## Notes

- This project code is available inside the container at `/workspace`.

- You can list running containers with:
```bash
docker ps
````

* You can enter at the running container with:

```bash
docker exec -it <container_id> bash
```

---

# 2 - Create Tables

To create the tables that we will use in HBase, inside the container:

1. Move to the workspace:

```bash
cd /workspace
```

2. Launch the script:

```bash
./hbase/create_tables.sh
```

---

# 3 - Load generated data to hdfs 

To load the generated csv data into Hdfs, in `/workspace` launch the script:

```bash
./data/load_to_hdfs.sh 
```

---

# 4 - MapReduce Jobs 

The MapReduce programs are written in Java. They include data processing jobs as well as a Bulk Load workflow for loading data into HBase by generating HFiles and directly importing them into HBase regions.

To build the Java MapReduce project and generate the JAR file (inside the container), run:

```bash
cd /workspace/mapreduce
mvn clean package # OR : mvn compile && mvn package
```

This generates a JAR file (in mapreduce/target/) similar to:

```text
mapreduce-1.0-SNAPSHOT.jar
```

You can execute any MapReduce job from inside the container using:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  <main-class> [input] [output]
```

The command depends on the driver class and arguments passed to the job.

## Examples

### 1. Bulk Load data into HBase

> Source code: `mapreduce/src/main/java/mapreduce/bulk_load`

#### 1.1 - Generate HFiles from a CSV dataset and store them in HDFS

Run the generic bulk load driver by providing:

- the input CSV file in HDFS
- the output HFiles directory in HDFS
- the target HBase table

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.bulk_load.BulkLoadDriver \
  <input_csv> \
  <unique_hfiles_output_dir> \
  <hbase_table>
```

Example for the visits table:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.bulk_load.BulkLoadDriver \
  /data/visits.csv \
  /tmp/hfiles_visits \
  web_site.visits
```

Example for the users table:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.bulk_load.BulkLoadDriver \
  /data/users.csv \
  /tmp/hfiles_users \
  web_site.users
```

#### 1.2 - Load generated HFiles into HBase

```bash
HADOOP_CLASSPATH="$HBASE_HOME/lib/shaded-clients/hbase-shaded-mapreduce-2.5.8.jar" \
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles \
  <hfiles_output_dir> \
  <hbase_table>
```

Example for the visits table:

```bash
HADOOP_CLASSPATH="$HBASE_HOME/lib/shaded-clients/hbase-shaded-mapreduce-2.5.8.jar" \
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles \
  /tmp/hfiles_visits \
  web_site.visits
```

Example for the users table:

```bash
HADOOP_CLASSPATH="$HBASE_HOME/lib/shaded-clients/hbase-shaded-mapreduce-2.5.8.jar" \
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles \
  /tmp/hfiles_users \
  web_site.users
```

#### 1.3 - Verify that the data has been loaded successfully
Example for `web_site.visits` table :

```bash
hbase shell
```

```ruby
$ count 'web_site.visits'
```

Example output:

```text
Current count: 1000, row: BE#0297#2025-03-31T13:42:49
Current count: 2000, row: DZ#0098#2025-08-05T00:52:22
Current count: 3000, row: DZ#0399#2025-01-13T03:22:00
Current count: 4000, row: ES#0195#2025-07-04T05:44:40
Current count: 5000, row: FR#0010#2025-06-10T20:57:45
Current count: 6000, row: FR#0322#2025-03-16T04:37:20
Current count: 7000, row: UK#0113#2025-05-12T00:34:09
Current count: 8000, row: UK#0417#2025-02-24T18:04:21
Current count: 9000, row: USA#0209#2025-11-19T14:43:31
Current count: 10000, row: USA#0500#2025-04-10T17:51:26

10000 row(s)
```

### 2. Compute the Number of Visits per Aggregation Column (Classic MR)

> Using a generic classic MapReduce job that reads data directly from HDFS csv file.
> Source code: `mapreduce/src/main/java/mapreduce/nb_visits_by_column_ClassicMR`

Supported aggregation columns (for `columnX`):

```text
country OR user_id OR page
```

Run the classic MapReduce job that save the result in hdfs:
```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.nb_visits_by_column_ClassicMR.Driver \
  /data/visits.csv \
  <columnX> \
  /nb_visits_per_<columnX>_ClassicMR_output 
```

Example: compute the number of visits per country 

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.nb_visits_by_column_ClassicMR.Driver \
  /data/visits.csv \
  country \
  /nb_visits_per_country_ClassicMR_output
```
---

### 3. Compute the Number of Visits per Aggregation Column (Table MapReduce)

> This generic Table MapReduce job reads data directly from the HBase table `web_site.visits` and computes the number of visits grouped by a specified column.
> The results can be stored either in **HDFS** or directly in an **HBase statistics table**.
>
> Source code:
>
> ```text
> mapreduce/src/main/java/mapreduce/nb_visits_by_column_TableMR
> ```

Supported aggregation columns (`columnX`):

```text
country OR user_id OR page
```

---

#### Run the Job and Store Results in HDFS

To save the aggregation results in HDFS, specify an output directory:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.nb_visits_by_column_TableMR.Driver \
  <columnX> \
  /nb_visits_per_<columnX>_TableMR_output
```

---

#### Run the Job and Store Results in HBase

To persist the aggregation results directly into an HBase table, omit the HDFS output path:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.nb_visits_by_column_TableMR.Driver \
  <columnX>
```

The results will be stored in one of the following HBase tables, depending on the selected aggregation column:

```text
web_site.stats_country OR web_site.stats_user_id OR web_site.stats_page
```

---

#### Example: Number of Visits per Country

##### Store Results in HDFS

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.nb_visits_by_column_TableMR.Driver \
  country \
  /nb_visits_per_country_TableMR_output
```

##### Store Results in HBase

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.nb_visits_by_column_TableMR.Driver \
  country
```

---

#### Results Verification (Visits per Country)

Display the output generated by the Classic MapReduce implementation:

```bash
hdfs dfs -cat /nb_visits_per_country_ClassicMR_output/part-r-00000
```

Display the output generated by the Table MapReduce implementation:

```bash
hdfs dfs -cat /nb_visits_per_country_TableMR_output/part-r-00000
```

Both implementations should produce identical results:

```text
BE      1667
DZ      1668
ES      1626
FR      1619
UK      1677
USA     1743
```

This confirms that the Table MapReduce implementation correctly reads data directly from HBase and produces the same aggregation results as the Classic MapReduce implementation

---

#### Verify the Results Stored in HBase

If the Table MapReduce job was executed without an HDFS output path, the results are written directly into an HBase statistics table.

Open the HBase shell with `hbase shell`, then scan the table with `scan 'web_site.stats_country'`


Example output:

```text
ROW    COLUMN+CELL
BE     column=stats:visit_count, value=\x00\x00\x06\x83
DZ     column=stats:visit_count, value=\x00\x00\x06\x84
...
```

The values appear in binary format because the JobReducerHbase stores the visit counts using:

```java
Bytes.toBytes(int)
```

Therefore, HBase displays the raw byte representation of the stored integers. Any application reading the data can deserialize these byte values into integer values to facilitate analysis, reporting, and debugging.


---
# TODO
* Run MapReduce jobs from hbase tables (join-phase 5)
* Benchmark perf





