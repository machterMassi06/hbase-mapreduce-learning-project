import json
import sys
import time

from spark_utils.session import create_spark_session
from pyspark.sql import functions as F


def thin_bulk_load(catalog_path, input_csv, output_path):
    """
    Execute a Spark-based HBase Thin Bulk Load pipeline.

    This pipeline follows the official HBase Spark integration model:
        1. Load HBase catalog (JSON)
        2. Build Spark DataFrame
        4. Use hbaseBulkLoadThinRows java wrapper to generate HFiles
        5. Load HFiles into HBase using LoadIncrementalHFiles

    Args:
        catalog_path (str): path to HBase Table JSON catalog definition
        input_csv : data source file 
        output_path (str): HDFS/local path where HFiles will be generated

    Returns:
        None
    """

    spark = create_spark_session(app_name="Thin Bulk Load")
    sc = spark.sparkContext
    jvm = sc._jvm

    # Java classes
    TableName = jvm.org.apache.hadoop.hbase.TableName


    # Load catalog
    with open(catalog_path, "r") as f:
        catalog = json.load(f)

    table_name = catalog["table"]["name"]
    columns = catalog["columns"]

    # Load CSV example ((replace with real source : parquet))

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

        n_partitions = 7 


    elif table_name == "web_site.users":

        df = df.withColumn(
            "key",
            F.col("user_id").cast("string")
        )

        n_partitions = 1 
    else:

        raise Exception(
            f"No rowkey strategy defined for {table_name}"
        )


    # HFiles must be sorted by rowkey
    # ThinBulkLoad handles the required sorting internally
    # 1 - Avoid global orderBy() as it triggers an expensive shuffle
    # df = df.orderBy("key")
    # OR  2- Using repartition(n, "key") to control parallelism and HFile generation
    # df = df.repartition(n_partitions, "key")


    # Convert catalog Python dict to Java Map

    java_columns = jvm.java.util.HashMap()


    for col_name, meta in columns.items():

        java_meta = jvm.java.util.HashMap()

        java_meta.put(
            "cf",
            meta["cf"]
        )

        java_meta.put(
            "col",
            meta["col"]
        )

        java_meta.put(
            "type",
            meta.get("type", "string")
        )

        java_columns.put(
            col_name,
            java_meta
        )


    # Initialize HBase context
    hbase_conf = sc._jsc.hadoopConfiguration()
    hbase_context = jvm.org.apache.hadoop.hbase.spark.HBaseContext(sc._jsc.sc(), hbase_conf, None)

    # Call my Java wrapper

    wrapper = (
        jvm.mapreduce.hbase.ThinBulkLoadWrapper
    )
    start = time.time()
    # Execute Thin Bulk Load (generate HFiles in "thin")
    wrapper.bulkLoadThinRows(
        hbase_context,
        df._jdf,
        java_columns,
        table_name,

        # Temporary directory where HFiles will be generated (staging area before loading into HBase)
        output_path,

        # HFile write options per column family (compression,bloom filters, block size, etc.)
        # Empty HashMap = default HBase settings
        jvm.java.util.HashMap(),

        # compactionExclude flag: True  -> HFiles are excluded from compactions , False -> normal HBase compaction behavior
        False,

        # Maximum HFile size in bytes (here: 256 MB)
        256 * 1024 * 1024
    )


    # Load generated HFiles into HBase

    connection_factory = jvm.org.apache.hadoop.hbase.client.ConnectionFactory
    conn = connection_factory.createConnection(hbase_conf)

    admin = conn.getAdmin()

    if not admin.tableExists(TableName.valueOf(table_name)):
        raise ValueError(f"Table {table_name} does not exist in HBase!")

    table = conn.getTable(TableName.valueOf(table_name))
    region_locator = conn.getRegionLocator(TableName.valueOf(table_name))

    load = jvm.org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles(hbase_conf)

    load.doBulkLoad(
        jvm.org.apache.hadoop.fs.Path(output_path),
        admin,
        table,
        region_locator
    )
    
    end = time.time()

    print(
        f"HBase bulk load duration: {end - start:.2f} seconds"
    )


    spark.stop()




if __name__ == "__main__":
    """

    Usage:
        python bulk_load.py <catalog.json> <input.csv> <output_path>

    Example:
        python bulk_load.py catalogs/visits.json data/visits.csv /tmp/hbase_hfiles
    """

    if len(sys.argv) != 4:
        print("Usage: thin_bulk_load.py <catalog.json> <input.csv> <output_path>")
        sys.exit(1)

    thin_bulk_load(sys.argv[1], sys.argv[2],sys.argv[3])