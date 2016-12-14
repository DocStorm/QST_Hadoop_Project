package Gid.Aid;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class UVCount {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf=new Configuration();
		Job job=Job.getInstance(conf);
		job.setJarByClass(UVCount.class);
		job.setMapperClass(Map.class);
		job.setMapOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setReducerClass(Reduce.class);
		job.setNumReduceTasks(2);
	
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);
		return;
	}
	
	public static class Map extends Mapper<LongWritable , Text, Text, IntWritable>{
		private final static IntWritable one = new IntWritable(1);
		public void map(LongWritable key, Text value,Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String pattern = "(\\d+.\\d+.\\d+.\\d+) [^ ]* [^ ]* \\[(.*):\\d+:\\d+:\\d+ [^ ]*\\] \"[^ ]+ ([^ ]+) .*";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(line);
			if(m.find()){
				context.write(new Text(m.group(1)),one);
			}
		}
	}
	
	public static class Reduce extends Reducer<Text , IntWritable, Text, IntWritable>{
		static int count = 0;
		public void reduce(Text key, Iterable<IntWritable> values,Context context)
				throws IOException, InterruptedException {
			
			count++;
		}
		public void cleanup(Context context) throws IOException, InterruptedException{
			String UV = "UV";
			context.write(new Text(UV), new IntWritable(count));
		}
	}
}
