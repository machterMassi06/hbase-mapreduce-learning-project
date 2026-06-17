package main.java.mapreduce.nb_visits_by_column_TableMR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.hbase.util.Bytes;

public class Driver {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println(
                "Usage: Driver <aggregate_column> [output_path]\n" +
                "If output_path is provided → results go to HDFS\n" +
                "If not → results go to HBase"
            );
            System.exit(1);
        }

        boolean persist_in_hbase_table = (args.length == 1);

        String aggregateColumn = args[0];

        if (!aggregateColumn.equals("country")
                && !aggregateColumn.equals("user_id")
                && !aggregateColumn.equals("page")) {

            System.err.println("aggregate.column must be: country, user_id, page");
            System.exit(1);
        }

        Configuration conf = HBaseConfiguration.create();
        conf.set("aggregate.column", aggregateColumn);

        Job job = Job.getInstance(conf, "Visits by " + aggregateColumn);
        job.setJarByClass(Driver.class);

        // Create a Scan object used to read data from the HBase table
        Scan scan = new Scan();
        // Only retrieve the requested column qualifier
        // (country or user_id or page, etc.) from the "info" column family.
        scan.addColumn(
            Bytes.toBytes("info"),
            Bytes.toBytes(aggregateColumn)
        );

        // Number of rows fetched per RPC call.
        // The default value is 1 returns row per call, which is inefficient for MapReduce jobs.
        // Increasing this value reduces network overhead.
        scan.setCaching(500);
        // Disable HBase block caching.
        // MapReduce jobs usually perform large sequential scans and do not
        // benefit from caching data in the RegionServer BlockCache.
        // This prevents useful cache space from being wasted.
        scan.setCacheBlocks(false);

        if (!persist_in_hbase_table) {

            // MODE HDFS OUTPUT
            TableMapReduceUtil.initTableMapperJob(
                "web_site.visits",
                scan,
                JobMapper.class,
                Text.class,
                IntWritable.class,
                job
            );

            job.setReducerClass(JobReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);

            FileOutputFormat.setOutputPath(job, new Path(args[1]));

        } else {
            // MODE HBASE OUTPUT

            // Let's assume that this table already exists and was created using the create_tables.sh script
            String tableName = "web_site.stats_" + aggregateColumn;

            TableMapReduceUtil.initTableMapperJob(
                "web_site.visits",
                scan,
                JobMapper.class,
                Text.class,
                IntWritable.class,
                job
            );

            TableMapReduceUtil.initTableReducerJob(
                tableName,
                JobReducerHbase.class,
                job
            );
        }

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}