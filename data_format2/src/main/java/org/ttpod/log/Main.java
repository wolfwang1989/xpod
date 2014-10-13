package org.ttpod.log;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by Administrator on 14-9-4.
 */
public class Main {

    public  static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        Job job = new Job(conf, "DataToRcfile" );
		/*
		 * 设置map
		 */
        job.setInputFormatClass(TextInputFormat.class);
        FileInputFormat.setInputPaths(job,new Path(args[0]));
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(BytesRefArrayWritable.class);
        job.setMapperClass(ParseMapper.class);
        job.setJarByClass(ParseMapper.class);
        /*
		 * 设置reduced
		 */
        job.setOutputFormatClass(org.ttpod.format.RCFileOutputFormat.class);
        FileOutputFormat.setOutputPath(job,new Path(args[1]));
        FileOutputFormat.setOutputCompressorClass(job, SnappyCodec.class);

        /*
		 * 相关配置
		 */
        job.getConfiguration().set(Constant.INPUT_CONF,args[2]);
        job.getConfiguration().set(Constant.OUTPUT_CONF,args[3]);
        job.getConfiguration().set(Constant.COLUMN_NUMBER_CONF_STR,args[4]);


        job.waitForCompletion(true);
        return;
    }
}
