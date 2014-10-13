package org.ttpod.log;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.hive.serde2.columnar.BytesRefWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.ttpod.record.FieldInfo;
import org.ttpod.record.FieldInfoFactory;
import org.ttpod.record.ILog;
import org.ttpod.record.Record;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 14-9-4.
 */
public class ParseMapper extends Mapper<LongWritable, Text, LongWritable, BytesRefArrayWritable> {

    static List<FieldInfo> FIELD_INFOS = null;
    static Logger log = Logger.getLogger(ParseMapper.class);
    static List<TableInfo.Field> OUTPUT_FIELDINFO = null;
    @Override
    public  void setup(Context context){
        Configuration conf = context.getConfiguration();
        if(FIELD_INFOS == null){
            synchronized(log) {
                FIELD_INFOS  = FieldInfoFactory.getFieldInfo(conf.get(Constant.INPUT_CONF));
            }
        }
        if(OUTPUT_FIELDINFO == null){
            synchronized (log){
                OUTPUT_FIELDINFO = TableInfo.getTableInfo(conf.get(Constant.OUTPUT_CONF),conf);
            }
        }
    }


    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        Record r = null;
        try {
             r = Record.createRecord(value.toString(),FIELD_INFOS);
            if(r == null)
                return;
        } catch (Exception e) {
            log.error(e);
            return;
        }
        List<ILog> ops = r.getLogList("request_body.data");
        int count = 0;
        byte[] val = null;
        BytesRefArrayWritable bytes = null;
        for(ILog op : ops){
            bytes = new BytesRefArrayWritable(OUTPUT_FIELDINFO.size());
            count = 0;
            for(TableInfo.Field field : OUTPUT_FIELDINFO){
                try{
                    val = field.getValue(op, r);
                    BytesRefWritable cu = new BytesRefWritable(val);
                    bytes.set(count++,cu);
                }catch (Exception e){
                    log.error("field " + field.name + " :" + e.getMessage());
                    bytes = null;
                    break;
                }
            }
            if(bytes != null)
                context.write(key,bytes);
        }
    }

}
