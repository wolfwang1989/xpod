package com.ttpod.loadIp.IPIndex;

import com.alibaba.fastjson.JSON;
import com.ttpod.loadIp.Location;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 14-5-19.
 */
public class CreateIndex {
    Connection conn ;
    ResultSet rs;
    String mysqlUrl;
    String username;
    String passwd;
    String outPut;
    boolean parseArgs(String[] args){
        if(args == null){
            System.out.println("please input correct args");
            return false;
        }
        for(int i = 0; i < args.length;++i){
            if(args[i].equals("-url")){
                mysqlUrl = args[++i];
            }
            else if(args[i].equals("-user")){
                username =  args[++i];
            }
            else if(args[i].equals("-p")){
                passwd =  args[++i];
            }
            else if(args[i].equals("-out")){
                outPut = args[++i];
            }

        }
        return outPut == null || mysqlUrl == null
                || username == null || passwd == null ? false:true;
    }
    public boolean init(String[] args) {
        if(!parseArgs(args)){
            System.out.println("error: args error");
            return false;
        }
        try {
            Class.forName("com.mysql.jdbc.Driver" );
            conn = DriverManager.getConnection(mysqlUrl + "?user=" + username + "&password=" + passwd + "&useUnicode=true&characterEncoding=utf-8");
            PreparedStatement stat = conn.prepareStatement("select begin_ip,end_ip,country,isp,city,province from ip_table");
            rs = stat.executeQuery();
            return true;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    void load() throws IOException {
        FileWriter f = new FileWriter(outPut);
        try {
            StringBuilder builder = new StringBuilder();
            Location loc = null;
            Map<String,String> m1 = new HashMap<String, String>(10);
            String address = null;
            List<test.SubNet> x = null;
            while(rs.next()){
                loc = new Location();
                loc.beginIp = rs.getString("begin_ip");
                loc.endIp = rs.getString("end_ip");
                loc.country = rs.getString("country");
                loc.province = rs.getString("province");
                loc.city = rs.getString("city");
                loc.isp = rs.getString("isp");
                m1.put("country",loc.country);
                m1.put("city",loc.city);
                m1.put("province",loc.province);
                m1.put("isp",loc.isp);
                address = JSON.toJSONString(m1);
                x = test.createSub(loc.beginIp, loc.endIp);
                if(x.isEmpty())
                    continue;

                for(test.SubNet sub:x ){
                    builder.delete(0,builder.capacity());
                    builder.append( "[\"").append(sub.startIp).append("/").append(Long.toString(sub.netMark)).append("\"") ;
                    builder.append(",").append("{\"address\":").append(address).append("}]\n");
                    f.write(builder.toString());
                }
            }
            f.close();
            rs.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        CreateIndex c = new CreateIndex();
        if(!c.init(args))
            return;
        c.load();
    }
}
