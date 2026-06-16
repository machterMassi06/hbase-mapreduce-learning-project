package main.java.mapreduce.bulk_load;

import java.io.IOException;

import javax.naming.Context;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Mapper;

public class BulkLoadMapper extends
        Mapper<LongWritable, Text, ImmutableBytesWritable, KeyValue> {
    
    private static final byte[] CF = Bytes.toBytes("info"); // the single column family of the table 

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        String line = value.toString();
        
        // ignore csv header
        if (line.startsWith("visit_id")){
            return ; 
        }

        String[] fields = line.split(",");

        if (fields.length != 5){
            return ; 
        }

        String visitId = fields[0].trim();
        String userId = fields[1].trim();
        String page = fields[2].trim();
        String country = fields[3].trim();
        String timestamp = fields[4].trim();

        
        /*
         * RowKey example (possible to evolve):
         * FR#0328#2025-05-21T08:54:58
         */
        String rowKeyString = country + "#" + userId + "#" + timestamp;
        byte[] rowKey = Bytes.toBytes(rowKeyString);
        ImmutableBytesWritable hKey = new ImmutableBytesWritable(rowKey);

        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("user_id"), Bytes.toBytes(userId)) 
        );


        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("page"), Bytes.toBytes(page)) 
        );

        
        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("country"), Bytes.toBytes(country)) 
        );


        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("timestamp"), Bytes.toBytes(timestamp)) 
        );



    }
    
}
