package com.ttpod.stat.hive.test;

import com.ttpod.stat.hive.udf.Location;
import com.ttpod.stat.hive.udf.Reader;

import java.io.File;
import java.io.IOException;

/**
 * Created by Administrator on 14-5-20.
 */
public class main {
    public static void  main(String[] args) throws IOException {
        Reader reader = new Reader(new File(args[0]));
        Location loc = reader.get(args[1]);
        if(loc != null){
            System.out.println("query success:");
            System.out.println("            city=" + loc.city + "; country=" + loc.country + "; province=" + loc.province + "; isp=" + loc.isp);
        }else{
            System.out.println("query failed:null");
        }
    }
}
