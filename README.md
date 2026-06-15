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
  hadoop-hbase-cluster
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

# 3 - Load generated data to hdfs 

To load the generated csv data into Hdfs, in `/workspace` launch the script:

```bash
./data/load_to_hdfs.sh 
```

---

# 2 - MapReduce

The MapReduce code is written in Java, including loading data into an HBase table using Bulk Load (directly loading HFiles into HBase regions).

To generate a JAR for the Java MapReduce code (outside the container), use the following command:

```bash
cd mapreduce
mvn compile
mvn package
```

This will generate a JAR file such as ~`mapreduce-1.0-SNAPSHOT.jar`, which you can execute with the following command inside the container:

```bash
hadoop jar /workspace/mapreduce/target/mapreduce-1.0-SNAPSHOT.jar mapreduce.<Class> [input] [output]
```


This command is generic and depends on the class and parameters you pass to it, but it is provided as a general example.

Exemple (inside the container : $/workspace): 

```bash 
hadoop jar mapreduce/target/mapreduce-1.0-SNAPSHOT.jar \
main.java.mapreduce.nb_visits_by_country.Driver \
/data/visits.csv \
/nb_visits_per_country_output
```

and the job result in : 

```bash 
$ hdfs dfs -cat /nb_visits_per_country_output/part-r-00000
BE      1667
DZ      1668
ES      1626
FR      1619
UK      1677
USA     1743
```
---

## TODO

* Load data into HBase using Java
* Run MapReduce jobs on HDFS





