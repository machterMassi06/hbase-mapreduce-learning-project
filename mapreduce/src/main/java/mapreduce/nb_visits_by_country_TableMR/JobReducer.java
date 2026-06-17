package main.java.mapreduce.nb_visits_by_country_TableMR;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

public class JobReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {

        int total = 0;

        for (IntWritable value : values) {
            total += value.get();
        }

        context.write(key, new IntWritable(total));
    }
}