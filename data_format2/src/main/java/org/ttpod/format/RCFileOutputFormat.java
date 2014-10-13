package org.ttpod.format;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.compress.DefaultCodec;

import org.apache.hadoop.hive.ql.io.RCFile;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.util.ReflectionUtils;

public class RCFileOutputFormat
        extends
        FileOutputFormat<WritableComparable<LongWritable>, BytesRefArrayWritable> {

    /**
     * set number of columns into the given configuration.
     *
     * @param conf
     *            configuration instance which need to set the column number
     * @param columnNum
     *            column number for RCFile's Writer
     *
     */
    public static void setColumnNumber(Configuration conf, int columnNum) {
        assert columnNum > 0;
        conf.setInt(RCFile.COLUMN_NUMBER_CONF_STR, columnNum);
    }

    /**
     * Returns the number of columns set in the conf for writers.
     *
     * @param conf
     * @return number of columns for RCFile's writer
     */
    public static int getColumnNumber(Configuration conf) {
        return conf.getInt(RCFile.COLUMN_NUMBER_CONF_STR, 0);
    }

    @Override
    public RecordWriter<WritableComparable<LongWritable>, BytesRefArrayWritable> getRecordWriter(
            TaskAttemptContext arg0) throws IOException, InterruptedException {
        Configuration conf = arg0.getConfiguration();
        conf.setBoolean("mapred.output.compress", true);
        Path outputPath = FileOutputFormat.getOutputPath(arg0);
        FileSystem fs = outputPath.getFileSystem(conf);
        if (!fs.exists(outputPath)) {
            fs.mkdirs(outputPath);
        }
        Path file = getDefaultWorkFile(arg0, "");
        CompressionCodec codec = null;
        if (getCompressOutput(arg0)) {
            Class<?> codecClass = getOutputCompressorClass(arg0,
                    DefaultCodec.class);
            codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass,
                    conf);
        }
        final RCFile.Writer out = new RCFile.Writer(fs, conf, file, null, codec);

        return new RecordWriter<WritableComparable<LongWritable>, BytesRefArrayWritable>() {

            @Override
            public void write(WritableComparable<LongWritable> key,
                              BytesRefArrayWritable value) throws IOException {
                out.append(value);
            }

            @Override
            public void close(TaskAttemptContext arg0) throws IOException,
                    InterruptedException {
                out.close();

            }
        };

    }
}