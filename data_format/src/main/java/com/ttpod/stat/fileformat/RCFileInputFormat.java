package com.ttpod.stat.fileformat;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class RCFileInputFormat<K extends LongWritable,V extends BytesRefArrayWritable>
extends FileInputFormat<LongWritable, BytesRefArrayWritable>
{

  @Override
  public RecordReader<LongWritable,BytesRefArrayWritable> createRecordReader(InputSplit split,
      TaskAttemptContext context) throws IOException, InterruptedException {

    context.setStatus(split.toString());
    return new RCFileRecordReader<LongWritable,BytesRefArrayWritable>();
  }

  @Override
  public List<InputSplit> getSplits(JobContext job) throws IOException {

    job.getConfiguration().setLong("mapred.min.split.size", SequenceFile.SYNC_INTERVAL);
    return super.getSplits(job);
  }
}
