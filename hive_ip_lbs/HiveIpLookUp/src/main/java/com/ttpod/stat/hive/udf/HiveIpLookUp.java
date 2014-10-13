package com.ttpod.stat.hive.udf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.io.IOException;
import java.util.ArrayList;

//import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Created by Administrator on 14-5-7.
 */
public class HiveIpLookUp extends UDF {
    static Log log = LogFactory.getLog(HiveIpLookUp.class);

    public final static String DEF_STR = "";
    private Reader reader;

    public HiveIpLookUp() throws IOException{
        this(new Configuration());
    }

    public HiveIpLookUp(Configuration conf) throws IOException{
        super();
        reader = ReaderFactory.getReader(conf);

    }

    public  ArrayList<String> evaluate(String ip) throws  IOException{
        Location location = reader.get(ip);
        if (location != null) {
            ArrayList<String> output = new ArrayList<String>(10);
            output.add(location.country != null ? location.country : DEF_STR);
            output.add(location.province != null ? location.province : DEF_STR);
            output.add(location.area != null ? location.area : DEF_STR);
            output.add(location.city != null ? location.city : DEF_STR);
            output.add(location.county != null ? location.county : DEF_STR);
            output.add(location.district != null ? location.district : DEF_STR);
            output.add(location.street != null ? location.street : DEF_STR);
            output.add(location.street_num != null ? location.street_num : DEF_STR);
            output.add(location.isp != null ? location.isp : DEF_STR);
            output.add(location.other != null ? location.other : DEF_STR);
            return output;
        }
        else{

        }
        return null;
    }

    static public void main(String[] args) throws IOException {
        Configuration conf = new Configuration();
        conf.set(ReaderFactory.DB_INEX_FILEPATH,"/usr/wolf/ip.data");
        HiveIpLookUp udf = new HiveIpLookUp();
        System.out.println(udf.evaluate(args[0]));
    }


}
