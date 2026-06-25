import json
import sys
from spark_utils.session import create_spark_session
from pyspark.sql import functions as F

def to_thin_row(row, columns):
    """
    Convert a Spark Row into HBase "thin bulk load" format.

    In the thin bulk load model, each row is represented as:
        (rowkey, [(family, qualifier, value), ...])

    This is more efficient than the basic bulk load model because:
        - all columns of a row are grouped together
        - reduces shuffle overhead in Spark
        - improves HFile generation performance

    Args:
        row: Spark Row object (input record)
        columns (dict): catalog column mapping:
            {
                column_name: {
                    "cf": column_family,
                    "col": qualifier,
                    "type": data_type
                }
            }

    Returns:
        tuple:
            (rowkey, list of (family, qualifier, value))

    """


    rowkey = row["key"]

    family_qual_values = []

    for col_name, meta in columns.items():

        if col_name == "key":
            continue

        cf = meta["cf"]
        qualifier = meta["col"]
        value = row[col_name]

        family_qual_values.append((cf, qualifier, value))

    return (rowkey, family_qual_values)

def thin_bulk_load(catalog_path, input_csv, output_path):
    """
    Execute a Spark-based HBase Thin Bulk Load pipeline.

    This pipeline follows the official HBase Spark integration model:
        1. Load HBase catalog (JSON)
        2. Build Spark DataFrame
        3. Transform data into thin row format:
               (rowkey, [(cf, qualifier, value), ...])
        4. Use hbaseBulkLoadThinRows to generate HFiles
        5. Load HFiles into HBase using LoadIncrementalHFiles

    Args:
        catalog_path (str): path to HBase Table JSON catalog definition
        output_path (str): HDFS/local path where HFiles will be generated

    Returns:
        None
    """

    spark = create_spark_session(app_name="Thin Bulk Load")
    sc = spark.sparkContext
    jvm = sc._jvm


    # Java classes
    TableName = jvm.org.apache.hadoop.hbase.TableName
    HBaseContext = jvm.org.apache.hadoop.hbase.spark.HBaseContext

    # Load catalog
    with open(catalog_path, "r") as f:
        catalog = json.load(f)

    table_name = catalog["table"]["name"]
    columns = catalog["columns"]

    # Example dataset csv (replace with real source : parquet)
    df = spark.read \
        .option("header", True) \
        .option("inferSchema", True) \
        .csv(input_csv)


    # ordering by rowkey is required for HFile generation

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

    elif table_name == "web_site.users":
        df = df.withColumn(
            "key",
            F.col("user_id")
        )

    else:
        raise Exception(f"No rowkey strategy defined for table {table_name}")

    df = df.orderBy("key")

    # Broadcast variables for distributed execution
    bc_columns = sc.broadcast(columns)

    # Transform DataFrame into thin row RDD
    # _jrdd to JavaRdd
    rdd = df.rdd.map(
        lambda row: to_thin_row(
            row,
            bc_columns.value
        )
    )

    # Initialize HBase context
    hbase_conf = sc._jsc.hadoopConfiguration()
    hbase_context = jvm.org.apache.hadoop.hbase.spark.HBaseContext(sc._jsc.sc(), hbase_conf, None)

    # Execute Thin Bulk Load (generate HFiles in "thin" mode without using MapReduce Jobs)
    hbase_context.hbaseBulkLoadThinRows(
        rdd._jrdd,
        TableName.valueOf(table_name),
        # Mapping function applied to each RDD element
        # Input: (rowkey, [(cf, qualifier, value), ...]), Output: same structure, sometimes normalized
        lambda t: (t[0], t[1]),

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
    conn = hbase_context.hbaseConnection()
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

    spark.stop()


# CLI ENTRYPOINT
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