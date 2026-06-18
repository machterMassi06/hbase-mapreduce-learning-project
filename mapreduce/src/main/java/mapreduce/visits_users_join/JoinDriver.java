package main.java.mapreduce.visits_users_join;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.mapreduce.Job;

import main.java.mapreduce.hbase_table_utils.HBaseTableUtils;

public class JoinDriver {

    public static void main(String[] args) throws Exception {

        Configuration conf = HBaseConfiguration.create();

        // CREATE TABLE that contains results of join 
        HBaseTableUtils.createTableIfNotExists(
                conf,
                "web_site.visits_enriched",
                "visit",
                "user"
        );

        // Join Job
        Job job = Job.getInstance(conf, "Join visits with users");

        job.setJarByClass(JoinDriver.class);

        Scan scan = new Scan();
        scan.setCaching(500);
        scan.setCacheBlocks(false);

        //The join is performed using the following strategy: 
        // 'web_site.visits' is the largest HBase table, so it's the table that will be read row by row by the mapper.
        // And since the users table is very small (500 rows), it is loaded only once in the mapper setup.
        // So we dont need to have a reducer Job 
        TableMapReduceUtil.initTableMapperJob(
                "web_site.visits",
                scan,
                JoinMapper.class,
                null,
                null,
                job
        );

        job.setNumReduceTasks(0);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}