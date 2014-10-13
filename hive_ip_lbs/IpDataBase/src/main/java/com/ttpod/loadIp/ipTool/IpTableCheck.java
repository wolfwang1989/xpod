package com.ttpod.loadIp.ipTool;

import com.ttpod.loadIp.Config;
import com.ttpod.loadIp.Location;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.sql.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Administrator on 14-6-27.
 */
public class IpTableCheck {
    static Log log = LogFactory.getLog(IpClear.class);

    static class IpComp implements Comparator<Location>{

        @Override
        public int compare(Location o1, Location o2) {
            if(o1.nEndIp < o2.nBeginIp){
                return -1;
            }
            else if(o1.nBeginIp > o2.nEndIp){
                return 1;
            }
            else
                return 0;
        }
    }
    Connection conn ;
    Config conf;
    Set<Location> locationSet = new TreeSet<Location>(new IpComp());

    public boolean init(Config conf) {
        this.conf = conf;
        try {
            Class.forName("com.mysql.jdbc.Driver" );
            String connStr = conf.mysqlUrl + "?user=" + conf.username + "&password=" + conf.passwd + "&useUnicode=true&characterEncoding=utf-8";

            conn = DriverManager.getConnection(connStr);
            conn.setAutoCommit(false);
            return true;
        } catch (ClassNotFoundException e) {
            log.error("",e);
        } catch (SQLException e) {
            log.error("", e);
        }
        return false;
    }

    public boolean check() throws SQLException, IOException {
        String sql = "select nbegin_ip,nend_ip,begin_ip,end_ip from ip_table  order by nbegin_ip; ";
        PreparedStatement statement = conn.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();
        Location last = null;
//        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("out.txt"));
        while(rs.next()){
            Location location = new Location();
            location.nBeginIp = rs.getLong("nbegin_ip");
            location.nEndIp = rs.getLong("nend_ip");
            location.beginIp = rs.getString("begin_ip");
            location.endIp = rs.getString("end_ip");

            if(locationSet.contains(location)){
                    System.out.print(last.toString());
                    System.out.print(location.toString());
                    System.out.println("\n");
                    return false;

//                return false;
            }
            last = location;
            locationSet.add(location);
        }
//        bufferedWriter.close();
        rs.close();
        return true;
    }

    public static void main(String[] args) throws SQLException, IOException {
        Config conf = new Config();
        conf.username = "wfp";
        conf.passwd = "wfp123456";
        conf.mysqlUrl = "jdbc:mysql://192.168.1.12:3306/test_wfp";
        IpTableCheck ipTableCheck = new IpTableCheck();
        ipTableCheck.init(conf);
        ipTableCheck.check();
    }
}
