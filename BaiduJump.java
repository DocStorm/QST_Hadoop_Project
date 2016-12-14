package Gid.Aid;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class BaiduJump {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf=new Configuration();
		Job job=Job.getInstance(conf);
		job.setJarByClass(BaiduJump.class);
		job.setMapperClass(Map.class);
		job.setMapOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setReducerClass(Reduce.class);
		job.setNumReduceTasks(1);
	
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);
		return;
	}
	
	public static class Map extends Mapper<LongWritable , Text, Text, Text>{
		private final static IntWritable one = new IntWritable(1);
		public void map(LongWritable key, Text value,Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String pattern = "(\\d+.\\d+.\\d+.\\d+) [^ ]* [^ ]* \\[(.*):\\d+:\\d+:\\d+ [^ ]*\\] \"[^ ]+ ([^ ]+) [^ ]+\" [^ ]+ [^ ]+ \"(http://[^ ]+.baidu.com)/[^ ]+\" +.*";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(line);
			if(m.find()){
				context.write(new Text(""),new Text(m.group(4)));
			}
		}
	}
	
	public static class Reduce extends Reducer<Text , Text, Text, IntWritable>{
		static int count = 0;
		public void reduce(Text key, Iterable<Text> values,Context context)
				throws IOException, InterruptedException {
			while(values.iterator().hasNext()){
				values.iterator().next();
				count++;
			}
		}
		public void cleanup(Context context) throws IOException, InterruptedException{
			String PV = "BaiduPV";
			context.write(new Text(PV), new IntWritable(count));
		}
	}
}
