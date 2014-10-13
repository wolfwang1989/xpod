package com.ttpod.stat.hive.io;


import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.io.LongWritable;  
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;  
import org.apache.hadoop.mapred.JobConf;  
import org.apache.hadoop.mapred.RecordReader;  
import org.apache.hadoop.mapred.Reporter;  
import org.apache.hadoop.mapred.TextInputFormat; 

public class MutilCharInputFormat extends TextInputFormat{
	private static final Log log = LogFactory.getLog(MutilCharRecordReader.class);

	@Override
	public RecordReader<LongWritable, Text> getRecordReader(
			InputSplit genericSplit, JobConf job, Reporter reporter)
			throws IOException {
		reporter.setStatus(genericSplit.toString());
	
		return new MutilCharRecordReader((FileSplit)genericSplit,job);
	}
	
}
