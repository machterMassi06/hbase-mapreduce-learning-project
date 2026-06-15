package main.java.mapreduce.nb_visits_by_country;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class JobMapper
        extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text countryKey = new Text();

    @Override
    protected void map(LongWritable key,
                       Text value,
                       Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        if (line.startsWith("visit_id")) {
            return;
        }

        String[] tokens = line.split(",");

        if (tokens.length == 5) {
            countryKey.set(tokens[3]);
            context.write(countryKey, ONE);
        }
    }
}