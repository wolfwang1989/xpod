package com.ttpod.loadIp.LoadCsvToTable;

import com.ttpod.loadIp.Config;
import com.ttpod.loadIp.Location;
import com.ttpod.loadIp.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Administrator on 14-6-27.
 */
public class Load {

    String fileName;
    String country;
    Config  conf;
    Connection conn;
    String province = "";
    static Log log = LogFactory.getLog(Load.class);

    public Load(String file, Config config,String coun,String pro){
        fileName = file;
        conf = config;
        country = coun;
        province = pro;

    }

    public boolean init(){
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
        return true;
    }
    static String sqlHead = "insert into ip_table_1 (begin_ip,end_ip,country,area,province,city,county,isp,other,nbegin_ip,nend_ip) ";
    void insert(Location ip) throws SQLException {
        PreparedStatement stat = conn.prepareStatement(sqlHead + ip.getValueSql());
        stat.executeUpdate();
        stat.close();
    }

    public boolean load() throws IOException, SQLException {
        File csv = new File(fileName); // CSV文件

        BufferedReader br = new BufferedReader(new FileReader(csv));
        String line = null;
        while((line = br.readLine()) != null){
           String trimStr = line.trim();
           if(trimStr.length() > 0){
               String[] fields =  trimStr.split(",");
               if(fields.length < 2)
                   continue;
               Location location = new Location();
               location.beginIp = fields[0];
               location.nBeginIp = Util.ipToLong(fields[0]);
               location.endIp = fields[1];
               location.nEndIp = Util.ipToLong(fields[1]);
               location.country = country;
               location.province = province;
               insert(location);
           }
        }
        br.close();
        return true;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Config conf = new Config();
        conf.username = "wfp";
        conf.passwd = "wfp123456";
        conf.mysqlUrl = "jdbc:mysql://192.168.1.12:3306/test_wfp";
        {
            Load load = new Load("./马来西亚.csv",conf,"马来西亚","");
            load.init();
            load.load();
        }
        {
            Load load = new Load("./新加坡.csv",conf,"新加坡","");
            load.init();
            load.load();
        }
        {
            Load load = new Load("./日本.csv",conf,"日本","");
            load.init();
            load.load();
        }
        {
            Load load = new Load("./香港.csv",conf,"中国","香港省");
            load.init();
            load.load();
        }
        {
            Load load = new Load("./台湾.csv",conf,"中国","台湾省");
            load.init();
            load.load();
        }

    }
}
