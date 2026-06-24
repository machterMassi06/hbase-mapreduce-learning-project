from pyspark.sql import SparkSession


def create_spark_session(app_name="HBaseReader"):
    return (
        SparkSession.builder
        .appName(app_name)
        .config(
            "spark.hadoop.hbase.zookeeper.quorum",
            "hadoop-hbase-cluster"
        )
        .config(
            "spark.hadoop.hbase.zookeeper.property.clientPort",
            "2181"
        )
        .getOrCreate()
    )