package Gid.Aid;

import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MapReduceHbase {
	public static void main(String[] args) throws MasterNotRunningException, ZooKeeperConnectionException,IOException, ClassNotFoundException, InterruptedException {
		String tablename = "zhangxuan";
		//Hbase Configuration
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.property.clientport", "2181");
        conf.set("hbase.zookeeper.quorum", "vm10-0-0-2.ksc.com");
        //conf.set("hbase.zookeeper.quorum.", "localhost"); 
        HBaseAdmin admin = new HBaseAdmin(conf);
        if(admin.tableExists(tablename)){
            System.out.println("table exists!recreating.......");
            admin.disableTable(tablename);
            admin.deleteTable(tablename);
        }
        HTableDescriptor htd = new HTableDescriptor(tablename);
        HColumnDescriptor hcd = new HColumnDescriptor("info");
        htd.addFamily(hcd);//创建列族
        admin.createTable(htd);//创建表
        System.out.println("table has been created");
        
        Job job=Job.getInstance(conf);
        job.setJarByClass(MapReduceHbase.class);
        job.setMapperClass(Map.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        //job.setReducerClass(HReduce.class);
        job.setNumReduceTasks(1);
        //which table
        TableMapReduceUtil.initTableReducerJob(tablename, HReduce.class, job);
        FileInputFormat.setInputPaths(job, new Path(args[0]));
		job.waitForCompletion(true);
		return;
    }
	
	public static class Map extends Mapper<LongWritable , Text, Text, IntWritable>{
		private final static IntWritable one = new IntWritable(1);
		public void map(LongWritable key, Text value,Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			String pattern = "(\\d+.\\d+.\\d+.\\d+) [^ ]* [^ ]* \\[(.*):\\d+:\\d+:\\d+ [^ ]*\\] \"[^ ]+ (/show/\\d+) .*";//musician
			Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(line);
				if(m.find()){
					context.write(new Text(m.group(3)),one);
				}	 
		}
	}
	
	public static class HReduce extends TableReducer<Text,IntWritable,ImmutableBytesWritable>{
		public void reduce(Text key, Iterable<IntWritable> values,Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			while(values.iterator().hasNext()) {
				sum += values.iterator().next().get();
			}
			Put put = new Put(key.getBytes());//put实例化，每个key存一行
			put.add(Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(String.valueOf(sum)));
			context.write(new ImmutableBytesWritable(key.getBytes()), put);
		}
	}
}
