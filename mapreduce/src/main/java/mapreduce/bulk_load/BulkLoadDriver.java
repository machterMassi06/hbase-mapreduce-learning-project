package main.java.mapreduce.bulk_load;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.hadoop.hbase.client.Table;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.KeyValue;

import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class BulkLoadDriver {
    
    public static void main(String[] args) throws Exception{

        if (args.length != 3) {
            System.err.println(
                "Usage: BulkLoadDriver <input> <output> <HbaseTable>"
            );
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath = args[1];
        String tableName = args[2];

        Configuration conf = HBaseConfiguration.create();
        
        Job job = Job.getInstance(conf, "Bulk load into "+ tableName);

        job.setJarByClass(BulkLoadDriver.class);

        // switch Mapper class (supported tables : visits & users)
        switch (tableName) {

            case "web_site.visits":
                job.setMapperClass(VisitsBulkLoadMapper.class);
                break;

            case "web_site.users":
                job.setMapperClass(UsersBulkLoadMapper.class);
                break;

            default:
                throw new IllegalArgumentException(
                    "Unknown table : " + tableName
                );
        }

        job.setMapOutputKeyClass(ImmutableBytesWritable.class);

        job.setMapOutputValueClass(KeyValue.class);

        FileInputFormat.addInputPath(job,new Path(inputPath));

        HFileOutputFormat2.setOutputPath(job, new Path(outputPath));

        Connection connection = ConnectionFactory.createConnection(conf);

        Table table = connection.getTable(TableName.valueOf(tableName));

        RegionLocator locator = connection.getRegionLocator(TableName.valueOf(tableName));

        HFileOutputFormat2.configureIncrementalLoad(job, table,locator);

        System.exit(job.waitForCompletion(true) ? 0 : 1);

        
    }
}
