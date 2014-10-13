package com.ttpod.loadIp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by wolf on 14-5-11.
 */
public class InsertImp implements InsertInterface {
    static Log log = LogFactory.getLog(InsertImp.class);
    Connection conn ;
    Config conf;
    public  InsertImp(){

        int a = 0;
    }

    @Override
    public boolean init(Config conf) {
        this.conf = conf;
        try {
            Class.forName("com.mysql.jdbc.Driver" );
            String connStr = conf.mysqlUrl + "?user=" + conf.username + "&password=" + conf.passwd + "&useUnicode=true&characterEncoding=utf-8";
            log.info(connStr);
            conn = DriverManager.getConnection(connStr);
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void Insert(Location ip) {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(Constant.IP_TABLE_NAME).append("(begin_ip,end_ip,country,area,province,city,county,isp,other,nbegin_ip,nend_ip)");
        builder.append(" values(");
        builder.append("'").append(ip.beginIp == null ? "":ip.beginIp).append("'").append(",");
        builder.append("'").append(ip.endIp == null ? "":ip.endIp).append("'").append(",");
        builder.append("'").append(ip.country == null ? "":ip.country).append("'").append(",");
        builder.append("'").append(ip.area == null ? "":ip.area).append("'").append(",");
        builder.append("'").append(ip.province == null ? "":ip.province).append("'").append(",");
        builder.append("'").append(ip.city == null ? "":ip.city).append("'").append(",");
        builder.append("'").append(ip.county == null ? "":ip.county).append("'").append(",");
        builder.append("'").append(ip.isp == null ? "":ip.isp).append("'").append(",");
        builder.append("'").append(ip.other == null ? "":ip.other).append("'").append(",");
        builder.append("'").append(ip.nBeginIp).append("'").append(",");
        builder.append("'").append(ip.nEndIp).append("'").append(")");
        try {
            PreparedStatement stat = conn.prepareStatement(builder.toString());
            stat.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage());

        }
    }

    @Override
    public void close() throws SQLException {
        conn.commit();
        conn.close();

    }
}
