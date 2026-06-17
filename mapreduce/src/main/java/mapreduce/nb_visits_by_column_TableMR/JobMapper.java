package main.java.mapreduce.nb_visits_by_column_TableMR;

import java.io.IOException;

import javax.naming.Context;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class JobMapper extends TableMapper<Text,IntWritable> {

    public static final byte[] CF = "info".getBytes();
    public  byte[] CQ;

    private final IntWritable ONE = new IntWritable(1);
    private Text text = new Text();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {

        String Cqualifier = context.getConfiguration().get("aggregate.column");

        CQ = Bytes.toBytes(Cqualifier);
    }
    
    public void map(ImmutableBytesWritable row, Result value, Context context)
            throws IOException, InterruptedException {
        
        String val = new String(value.getValue(CF, CQ));
        text.set(val);
        context.write(text, ONE);

    }

}
