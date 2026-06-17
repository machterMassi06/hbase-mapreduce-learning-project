package main.java.mapreduce.nb_visits_by_column_ClassicMR;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

import javax.naming.Context;


public class JobMapper
        extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text columnKey = new Text();
    private int columnIndex;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {

        String aggregateColumn = context.getConfiguration().get("aggregate.column");

        switch (aggregateColumn) {

            case "user_id":
                columnIndex = 1;
                break;

            case "page":
                columnIndex = 2;
                break;

            case "country":
                columnIndex = 3;
                break;

            default:
                throw new IOException(
                    "Unsupported aggregation column: "
                    + aggregateColumn
                );
        }
    }

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

        if (tokens.length != 5) {
            return;
        }

        columnKey.set(tokens[columnIndex]);
        context.write(columnKey,ONE);
    }
}