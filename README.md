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
git clone <REPO_URL>
cd hbase_mapreduce_project
```

---

## Start a ready-to-use Hadoop + HBase pseudo distr cluster 

To avoid setup, you can use [my Docker-based cluster repo](https://github.com/machterMassi06/Hbase-pseudo-distributed) (pseudo-distributed mode) that includes: HDFS,YARN, HBase; and follow its README to build the image.

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

---

## TODO

* Load data into HBase using Java
* Run MapReduce jobs on HDFS





