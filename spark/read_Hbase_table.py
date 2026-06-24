import sys
import json

from spark_utils.session import create_spark_session


def read_hbase_table(catalog_path):
    spark = create_spark_session("ReadHBaseTable")

    with open(catalog_path, "r") as f:
        catalog = json.dumps(json.load(f))

    df = (
        spark.read
        .format("org.apache.hadoop.hbase.spark")
        .option("catalog", catalog)
        .load()
    )

    df.show(truncate=False)

    spark.stop()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(
            f"Usage: {sys.argv[0]} <path to catalog.json>"
        )
        sys.exit(1)

    catalog_path = sys.argv[1]

    read_hbase_table(catalog_path)