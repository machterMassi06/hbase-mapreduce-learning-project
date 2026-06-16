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
./hbase/create_tables.sh $HBASE_HOME
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
mvn compile
mvn package
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
> code src in : mapreduce/src/main/java/mapreduce/bulk_load

Generate HFiles from the CSV dataset and store them in HDFS:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.bulk_load.BulkLoadDriver \
  /data/visits.csv \
  /tmp/hfiles
```

Load the generated HFiles into the HBase table:

```bash
HADOOP_CLASSPATH="$HBASE_HOME/lib/shaded-clients/hbase-shaded-mapreduce-2.5.8.jar" \
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles \
  /tmp/hfiles \
  web_site.visits
```

Verify that the data has been loaded successfully:

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


### 2. Compute the Number of Visits per Country
> code src in : mapreduce/src/main/java/mapreduce/nb_visits_by_country 

Run the MapReduce job:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
  main.java.mapreduce.nb_visits_by_country.Driver \
  /data/visits.csv \
  /nb_visits_per_country_output
```

Display the generated results:

```bash
hdfs dfs -cat /nb_visits_per_country_output/part-r-00000
```

Output:

```text
BE      1667
DZ      1668
ES      1626
FR      1619
UK      1677
USA     1743
```

The output contains the total number of visits recorded for each country in the dataset.

---
# TODO
* Run MapReduce jobs from hbase tables (ETL, aggreg, join...)





