package com.ttpod.stat.hive.udf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;

/**
 * Created by Administrator on 14-5-7.
 */
public class ReaderFactory {
    static Log log = LogFactory.getLog(ReaderFactory.class);
    public static  final String DB_INEX_FILEPATH = "org.ttpod.ip.dbdata.path";
    private static Reader reader = null;

    public static synchronized Reader getReader(Configuration conf) throws IOException{
        if(reader == null){
            String fileName = conf.get(DB_INEX_FILEPATH);
            fileName = "hdfs:/user/wolf/my_ip.ttdb";
            FileSystem fs = FileSystem.get(URI.create(fileName),conf);
            org.apache.hadoop.fs.FSDataInputStream inputStream = null;
            try{
                inputStream = fs.open(new Path(fileName));

            }catch (IOException e){
                log.error(e.getMessage());
                return null;
            }
            ThreadBuffer buffer = new ThreadBuffer(inputStream);
            inputStream.close();
            try {
                reader = new Reader(buffer,fileName);
            }
            catch (IOException e){
                log.error(e.getMessage());
                return null;
            }catch (Exception e){
                log.error(e.getMessage());
                return null;
            }
        }
        return reader;
    }

}
