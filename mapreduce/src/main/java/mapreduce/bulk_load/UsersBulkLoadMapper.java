package main.java.mapreduce.bulk_load;

import java.io.IOException;

import javax.naming.Context;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Mapper;

public class UsersBulkLoadMapper extends
        Mapper<LongWritable, Text, ImmutableBytesWritable, KeyValue> {
    
    private static final byte[] CF = Bytes.toBytes("info"); // the single column family of the table 

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        String line = value.toString();
        
        // ignore csv header
        if (line.startsWith("user_id")){
            return ; 
        }

        String[] fields = line.split(",");

        if (fields.length != 5){
            return ; 
        }

        String userId = fields[0].trim();
        String firstName = fields[1].trim();
        String lastName = fields[2].trim();
        String age = fields[3].trim();
        String gender = fields[4].trim();

        
        /*
         * RowKey = user_id (possible to evolve), example :
         * 0134
         */
        String rowKeyString = userId;
        byte[] rowKey = Bytes.toBytes(rowKeyString);
        ImmutableBytesWritable hKey = new ImmutableBytesWritable(rowKey);

        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("first_name"), Bytes.toBytes(firstName)) 
        );


        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("last_name"), Bytes.toBytes(lastName)) 
        );

        
        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("age"), Bytes.toBytes(age)) 
        );


        context.write(
            hKey,
            new KeyValue(rowKey, CF, Bytes.toBytes("gender"), Bytes.toBytes(gender)) 
        );



    }
    
}
