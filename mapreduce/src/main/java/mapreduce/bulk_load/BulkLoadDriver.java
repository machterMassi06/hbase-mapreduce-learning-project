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

        Configuration conf = HBaseConfiguration.create();
        
        Job job = Job.getInstance(conf, "Bulk load visits from hdfs");

        job.setJarByClass(BulkLoadDriver.class);

        job.setMapperClass(BulkLoadMapper.class);

        job.setMapOutputKeyClass(ImmutableBytesWritable.class);

        job.setMapOutputValueClass(KeyValue.class);

        FileInputFormat.addInputPath(job,new Path(args[0]));

        Path outputPath = new Path(args[1]);

        HFileOutputFormat2.setOutputPath(job, outputPath);

        Connection connection = ConnectionFactory.createConnection(conf);

        Table table = connection.getTable(TableName.valueOf("web_site.visits"));

        RegionLocator locator = connection.getRegionLocator(TableName.valueOf("web_site.visits"));

        HFileOutputFormat2.configureIncrementalLoad(job, table,locator);

        System.exit(job.waitForCompletion(true) ? 0 : 1);

        
    }
}
