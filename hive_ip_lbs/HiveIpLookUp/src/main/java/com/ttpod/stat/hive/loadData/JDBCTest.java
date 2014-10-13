package com.ttpod.stat.hive.loadData;

/**
 * Created by Administrator on 14-5-12.
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCTest {
    public static void main(String[] args) {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://192.168.1.12:3306/test_wfp";
        String user = "wfp";
        String password = "wfp123456";

        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url+"?user="+user+"&password="+password+"&useUnicode=true&characterEncoding=utf8");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("insert into ip_table(begin_ip,end_ip,country,area,province,city,county,isp,other) values('199.233.160.0','199.233.160.21','fuck','','','','','','')");
            ResultSet rs = stmt.executeQuery("select * from messages");
            while (rs.next()) {
                //int id = rs.getInt("id");
                String message = rs.getString("message");
                System.out.println(" " + message);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
