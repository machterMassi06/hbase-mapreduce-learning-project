package main.java.mapreduce.nb_visits_by_column_TableMR;

import java.io.IOException;

import javax.naming.Context;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.hbase.util.Bytes; 

public class JobReducerHbase extends TableReducer<Text, IntWritable, ImmutableBytesWritable> {
    
    public static final byte[] CF = Bytes.toBytes("stats");
    public static final byte[] CQ = Bytes.toBytes("visit_count");

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {

        int sum = 0;

        for (IntWritable val : values) {
            sum += val.get();
        }

        // RowKey = map output key = country | user_id | page
        byte[] rowKey = Bytes.toBytes(key.toString());
        Put put = new Put(rowKey);

        // column family = stats , column qualifier = visit_count 
        put.addColumn(CF, CQ, Bytes.toBytes(sum));

        context.write(new ImmutableBytesWritable(rowKey), put);
    }
}