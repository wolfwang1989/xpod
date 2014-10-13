package com.ttpod.loadIp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-5-15.
 * 插入ip信息到ip库中，要进行合并、去重操作
 */
public class MergeInsertImp implements InsertInterface{
    static Log log = LogFactory.getLog(MergeInsertImp.class);
    Connection conn ;
    Config conf;
    ResultSet rs;
    Location current =  null;

    List<String> sqlStringList = new ArrayList<String>(20000);

    public  MergeInsertImp(){

    }

    @Override
    public boolean init(Config conf) {
        this.conf = conf;
        try {
            Class.forName("com.mysql.jdbc.Driver" );
            conn = DriverManager.getConnection(conf.mysqlUrl + "?user=" + conf.username + "&password=" + conf.passwd + "&useUnicode=true&characterEncoding=utf-8");
            conn.setAutoCommit(false);
            PreparedStatement stat = conn.prepareStatement("select other,county,area,begin_ip,end_ip,nbegin_ip,nend_ip,begin_ip,end_ip,country,province,city,isp from ip_table order by nbegin_ip");
            rs = stat.executeQuery();

            return true;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    Location getNext(){

        try {
            Location ret = new Location();
            ret.nBeginIp = rs.getLong("nbegin_ip");
            ret.nEndIp = rs.getLong("nend_ip");
            ret.area = rs.getString("area");
            ret.beginIp = rs.getString("begin_ip");
            ret.endIp = rs.getString("end_ip");
            ret.country = rs.getString("country");
            ret.province = rs.getString("province");
            ret.county = rs.getString("county");
            ret.other = rs.getString("other");
            ret.city = rs.getString("city");
            ret.isp = rs.getString("isp");
            return ret;

        } catch (SQLException e) {
            log.error("query exception",e);
        }
        return null;
    }
    void insert(Location ip){
        ip.beginIp = Util.longToIP(ip.nBeginIp);
        ip.endIp = Util.longToIP(ip.nEndIp);
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
        sqlStringList.add(builder.toString());
    }
    void delete(Location ip){
        String sql = "delete from ip_table  where nbegin_ip = " + ip.nBeginIp;
        sqlStringList.add(sql);
    }
    @Override
    public void Insert(Location toMerge) {
       while (true){
            try {
                if(current == null && rs.next()){
                    current = getNext();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(current == null){
                insert(toMerge);
                return;
            }
            if(current.beginIp.equals("222.161.60.115")){
                System.out.println("error");
            }
            //     s  e
            //s  e       cuurrent keep used for next tomergeip
            if(toMerge.nEndIp < current.nBeginIp){
                insert(toMerge);
            }
            //     s   e
            //           s  e  drop current ,get next current
            else if(toMerge.nBeginIp > current.nEndIp){
                current = null;
                continue;
            }
            //     s   e
            //   s  e s  e
            else{
                toMerge = merge(toMerge);
                if(toMerge != null)
                    continue;
            }
           return;
       }
    }

    /*
     *  true:continue merge,false:end
     */
    Location merge(Location toMerge)  {
        delete(current);
        StringBuilder builder = new StringBuilder();
        builder.append("country:" + current.country).append(",").append("province:" + current.province).append(",");
        if(toMerge.nBeginIp <= current.nBeginIp){
            builder.append("begin_ip:" + Util.longToIP(current.nBeginIp)).append(",");
        }
        else
            builder.append("begin_ip:" + Util.longToIP(toMerge.nBeginIp)).append(",");

        if(toMerge.nEndIp >= current.nEndIp){
            builder.append("end_ip:" + Util.longToIP(current.nEndIp)).append(",");
        }
        else
            builder.append("end_ip:" + Util.longToIP(toMerge.nEndIp)).append(",");

        try {
            if(current.country.equals("中国")){
                builder.append("======> " ).append("newCountry:" + toMerge.country).append(" , newProvince:" ).append(toMerge.province);
                FileWriter f = new FileWriter("out_1.txt",true);
                f.write(builder.toString() + "\n");
                f.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //  s   e
        // s       e
        if(toMerge.nEndIp >= current.nEndIp &&  toMerge.nBeginIp <= current.nBeginIp){
            Location one = null;
            Location two = null;
            Location three = null;

                one = new Location(toMerge);
                one.nEndIp = current.nEndIp ;
                one.endIp = Util.longToIP(one.nEndIp);
                insert(one);
            if(toMerge.nEndIp != current.nEndIp){
                two = new Location(toMerge);
                two.nBeginIp = current.nEndIp + 1;
                two.beginIp = Util.longToIP(two.nBeginIp);
            }
            current = null;
            return two;
        }
        // s    e
        //   s e
        else if(toMerge.nEndIp <= current.nEndIp && toMerge.nBeginIp >= current.nBeginIp ){
            Location one = null;
            Location two = null;
            Location three = null;
            if(current.nBeginIp != toMerge.nBeginIp){
                one = new Location(current);
                one.nEndIp = toMerge.nBeginIp -1;
                one.endIp = Util.longToIP(one.nEndIp);
                insert(one);
            }

            two = toMerge;
            insert(two);
            if(current.nEndIp != toMerge.nEndIp){
                current.nBeginIp = toMerge.nEndIp + 1;
                current.beginIp = Util.longToIP(current.nBeginIp);
                insert(current);
            }
            else
                current = null;
            return null;
        }
        //    s    e
        //  s   e
        else if(toMerge.nBeginIp < current.nBeginIp && toMerge.nEndIp < current.nEndIp  && toMerge.nEndIp >= current.nBeginIp ){
            insert(toMerge);
            current.nBeginIp = toMerge.nEndIp + 1;
            current.beginIp = Util.longToIP(current.nBeginIp);
            insert(current);
            return null;
        }
        //    s    e
        //      s     e
        else if(toMerge.nEndIp > current.nEndIp && toMerge.nBeginIp > current.nBeginIp && toMerge.nBeginIp <= current.nEndIp){
            current.nEndIp = toMerge.nBeginIp -1;
            current.endIp = Util.longToIP(current.nEndIp);
            insert(current);

            current = null;
            return toMerge;
        }
        else {
            log.error(" wrong ip");
        }
        return null;
    }

    @Override
    public void close() throws SQLException {
//        log.info("begin commit sql,sql length = " + sqlStringList.size());
//
//        for(String sql :sqlStringList){
//            PreparedStatement statement1 = conn.prepareStatement(sql);
//            statement1.execute();
//            statement1.close();
//        }
//        log.info("end commit sql,sql length = " + sqlStringList.size());
//        conn.commit();
//        conn.close();
    }
}
