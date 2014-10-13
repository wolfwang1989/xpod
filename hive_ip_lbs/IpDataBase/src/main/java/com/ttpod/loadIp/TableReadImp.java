package com.ttpod.loadIp;

import com.mysql.jdbc.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Administrator on 14-6-27.
 */
public class TableReadImp implements ReadInterface {

    static Log log = LogFactory.getLog(TableReadImp.class);

    String tableName;
    Config conf;
    java.sql.Connection conn;
    private ResultSet rs;

    public TableReadImp(){

    }

    @Override
    public boolean init(Config conf) {
        this.conf = conf;
        tableName = conf.inputTable;
        try {
            Class.forName("com.mysql.jdbc.Driver" );
            String connStr = conf.mysqlUrl + "?user=" + conf.username + "&password=" + conf.passwd + "&useUnicode=true&characterEncoding=utf-8";

            conn = DriverManager.getConnection(connStr);
            conn.setAutoCommit(false);
            PreparedStatement stat = conn.prepareStatement("select * from " + tableName + " order by nbegin_ip");
            rs = stat.executeQuery();
            return true;
        } catch (ClassNotFoundException e) {
            log.error("",e);
        } catch (SQLException e) {
            log.error("", e);
        }

        return false;
    }

    @Override
    public Location read() {
        Location ip = new Location();

        try {
            ip.beginIp = rs.getString("begin_ip");
            ip.endIp = rs.getString("end_ip");
            ip.nBeginIp = rs.getLong("nbegin_ip");
            ip.nEndIp = rs.getLong("nend_ip");
            ip.country = rs.getString("country");
            ip.province = rs.getString("province");
            ip.city = rs.getString("city");
            ip.isp = rs.getString("isp");
        } catch (SQLException e) {
            log.error("",e);
            return null;
        }
        return ip;
    }

    @Override
    public boolean hasNext() {
        try {
            return rs.next();
        } catch (SQLException e) {
            log.error("",e);
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            rs.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
