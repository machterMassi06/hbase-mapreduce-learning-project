package main.java.mapreduce.nb_visits_by_column_ClassicMR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Driver {
    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println(
                "Usage: Driver <input_path> <aggregate_column> <output_path>"
            );
            System.exit(1);
        }

        Configuration conf = new Configuration();
                String aggregateColumn = args[1]; // nb visits by aggregateColumn 

        if (!aggregateColumn.equals("country")
                && !aggregateColumn.equals("user_id")
                && !aggregateColumn.equals("page")) {

            System.err.println(
                "aggregate.column must be one of: country, user_id, page"
            );

            System.exit(1);
        }

        conf.set("aggregate.column", aggregateColumn);

        
        Job job = Job.getInstance(conf, "Visits by " + aggregateColumn);

        job.setJarByClass(Driver.class);
        job.setMapperClass(JobMapper.class);
        job.setReducerClass(JobReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setInputFormatClass(TextInputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);
    }
}