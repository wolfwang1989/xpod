package com.ttpod.stat.hive.test;

import com.ttpod.stat.hive.udf.Location;
import com.ttpod.stat.hive.udf.Reader;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Administrator on 14-5-20.
 */
public class testDb {
    static boolean eof = false;
    static StringBuilder courrent = new StringBuilder();
    static String readLine(FileReader inputFile ) throws IOException {
        if(eof)
            return null;
        courrent.delete(0,courrent.capacity());
        int c;
        while((c =  inputFile.read()) != -1 ){

            if(c == '\n'){
                break;
            }else{
                courrent.append((char)c);
            }
        }
        if(c == -1)
            eof = true;
        return courrent.toString();
    }
    static public void main(String[] args) throws IOException, JSONException {
        Reader reader = new Reader(new File(args[1]));
        FileReader f = new FileReader(args[0]);
        String line = null;
        String[] ips = null;
        while(true){
            line = readLine(f);
//            System.out.println(line);
            if(line == null)
                break;
            if(line.trim().length() == 0){
                continue;
            }
            JSONArray array = new JSONArray(line);
            ips = array.getString(0).split("/");
            String country = array.getJSONObject(1).getJSONObject("address").getString("country");
            Location loc = reader.get(ips[0]);
            if(null == loc || !loc.country.equals(country) ){
                System.out.println("test error!!!");
                System.out.println("error line:" + line);
                return;
            }
        }
        f.close();
        System.out.println("test success!!!");
    }

}
