package Gid.Aid;

import java.io.File;
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
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class NextDaySaver {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf=new Configuration();
		conf.set("s", args[0]);
		Job job=Job.getInstance(conf);
		job.setJarByClass(NextDaySaver.class);
		job.setMapperClass(Map.class);
		job.setMapOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setReducerClass(Reduce.class);
		job.setNumReduceTasks(2);
	
		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		job.waitForCompletion(true);
		return;
	}
	
	public static class Map extends Mapper<LongWritable , Text, Text, IntWritable>{
		private static Integer storm = 0;
		public void setup(Context context)throws IOException{
			Configuration conf = context.getConfiguration();
			String name = ((FileSplit)context.getInputSplit()).getPath().toUri().getPath();
			String path = name.substring(0, name.lastIndexOf('/'));
			System.out.println(path);
			if(path.equals(conf.get("s"))){
				storm = 1;
			}else{
				storm = 2;
			}
		}
		public void map(LongWritable key, Text value,Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			
			String pattern = "(\\d+.\\d+.\\d+.\\d+) [^ ]* [^ ]* \\[(.*):\\d+:\\d+:\\d+ [^ ]*\\] \"[^ ]+ ([^ ]+) .*";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(line);
			if(m.find()){
				context.write(new Text(m.group(1)),new IntWritable(storm));
			}
		}
	}
	
	public static class Reduce extends Reducer<Text , IntWritable, Text, IntWritable>{
		static int count = 0;
		public void reduce(Text key, Iterable<IntWritable> values,Context context)
				throws IOException, InterruptedException {
			boolean a = false;
			boolean b = false;
			while(values.iterator().hasNext()){
				int type = values.iterator().next().get();
				if(type == 1){
					a = true;
				}
				if(type == 2){
					b = true;
				}
				if(a == true && b == true){
					count++;
					break;
				}
			}			
		}
		public void cleanup(Context context) throws IOException, InterruptedException{
			String NextDaySaver = "NextDaySaver";
			context.write(new Text(NextDaySaver), new IntWritable(count));
		}
	}
}
