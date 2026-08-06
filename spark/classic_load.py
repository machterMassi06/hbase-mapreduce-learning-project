import json
import sys
import time

from spark_utils.session import create_spark_session
from pyspark.sql import functions as F


def classic_write(catalog_path, input_csv):
    """
    Execute a standard HBase-Spark Connector write.

    Pipeline:
        1. Load HBase catalog
        2. Read source CSV
        3. Build HBase rowkey
        4. Write DataFrame using HBase-Spark Connector

    This is used as a comparison baseline against ThinBulkLoad.
    """

    spark = create_spark_session(app_name="HBase Connector Write")

    # Load catalog
    with open(catalog_path, "r") as f:
        catalog = json.load(f)

    table_name = catalog["table"]["name"]

    # Read source data
    df = (
        spark.read
        .option("header", True)
        .option("inferSchema", True)
        .csv(input_csv)
    )


    # Build HBase rowkey
    if table_name == "web_site.visits":

        df = df.withColumn(
            "key",
            F.concat_ws(
                "#",
                F.col("country"),
                F.col("user_id"),
                F.col("timestamp")
            )
        )
        df = df.drop("visit_id")

    elif table_name == "web_site.users":

        df = df.withColumn(
            "key",
            F.col("user_id").cast("string")
        )

    else:
        raise Exception(
            f"No rowkey strategy defined for {table_name}"
        )


    catalog_json = json.dumps(catalog)

    start = time.time()

    
    (
        df.write
        .format("org.apache.hadoop.hbase.spark")
        .option(
            "catalog",
            catalog_json
        )
        .mode("append")
        .save()
    )


    end = time.time()

    print(
        f"HBase classic load duration: {end - start:.2f} seconds"
    )


    spark.stop()



if __name__ == "__main__":

    if len(sys.argv) != 3:
        print(
            "Usage: classic_load.py <catalog.json> <input.csv>"
        )
        sys.exit(1)


    classic_write(
        sys.argv[1],
        sys.argv[2]
    )