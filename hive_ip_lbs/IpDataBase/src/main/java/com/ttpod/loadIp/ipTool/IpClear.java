package com.ttpod.loadIp.ipTool;

import com.ttpod.loadIp.Config;
import com.ttpod.loadIp.Location;
import com.ttpod.loadIp.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by Administrator on 14-6-26.
 */
public class IpClear {
    static class LocComprator implements Comparator<Location>{

        @Override
        public int compare(Location o1, Location o2) {
            if(o1.nBeginIp < o2.nBeginIp)
                return -1;
            else if(o1.nBeginIp == o2.nBeginIp)
                return 0;
            else
                return 1;
        }
    }

    static Log log = LogFactory.getLog(IpClear.class);

    Connection conn ;
    Config conf;
    ResultSet rs;
    Location last;
    List<Location> ordLocations = new ArrayList<Location>();
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

    void extract() throws SQLException {
        String sql = "select begin_ip,end_ip,country,area,province,city,county,isp,nbegin_ip,nend_ip from cdn_mix order by nbegin_ip; ";
        PreparedStatement statement = conn.prepareStatement(sql);
        rs = statement.executeQuery();
    }

    void transfer() throws SQLException {
        int count = 0;
        boolean isHaveNext = true;
        while((isHaveNext && (isHaveNext = rs.next()) == true ) || !ordLocations.isEmpty() ){
            Location current = null;
            Location ip = new Location();
            if(isHaveNext){
                ip.beginIp = rs.getString("begin_ip");
                ip.endIp = rs.getString("end_ip");
                ip.nBeginIp = rs.getLong("nbegin_ip");
                ip.nEndIp = rs.getLong("nend_ip");
                ip.country = rs.getString("country");
                ip.province = rs.getString("province");
                ip.city = rs.getString("city");
                ip.isp = rs.getString("isp");
            }
            if(ordLocations.isEmpty())
                current = ip;
            else{
                if(isHaveNext){
                    ordLocations.add(ip);
                }
                Collections.sort(ordLocations,new LocComprator());
                current = ordLocations.get(0);
                ordLocations.remove(0);
            }
            if(last == null){
                last = current;
            }
            else if(current.nEndIp <= last.nEndIp){//内包含，切割成2或3
//                System.out.println("内包含");
//                System.out.println("last begin_ip:" + last.beginIp + "; end_ip:" + last.endIp);
//                System.out.println("current begin_ip:" + current.beginIp + "; end_ip:" + current.endIp);
                Location one = null;
                Location two = null;
                Location three = null;

                if(current.nBeginIp != last.nBeginIp){
                    one = new Location(last);
                    one.nEndIp = current.nBeginIp - 1;
                    one.endIp = Util.longToIP(one.nEndIp);
                }
                two  = new Location(current);
                if(current.nEndIp != last.nEndIp){
                    three = new Location(last);
                    three.nBeginIp = current.nEndIp + 1;
                    three.beginIp = Util.longToIP(three.nBeginIp);
                }
                if(one != null && three != null){
                    System.out.println("plus one");
                    count++;
                }
                if(one == null && three == null){
                    System.out.println("decr one");
                    count--;
                }
                load(one);

                if(three != null)
                    ordLocations.add(three);
//                if(last.country.equals("中国") || current.country.equals("中国")){
//                    System.out.println(last.country + "," + last.province + ":" + last.beginIp + "\\" + last.endIp + "--"
//                            + current.country + "," + current.province + ":" + current.beginIp + "\\" + current.endIp
//                        + "==> " + (one == null? "": (one.beginIp + "\\" + one.endIp))  + "," + (two.beginIp + "\\" + two.endIp) + ","
//                            + (three == null ? "" : (three.beginIp + "\\" + three.endIp)));
//                }
//                if(!last.country.equals(current.country)){
//                    System.out.println("ERROR:" + last.country + "," + last.province + ":" + last.beginIp + "\\" + last.endIp + "--"
//                            + current.country + "," + current.province + ":" + current.beginIp + "\\" + current.endIp);
//                }
                last = two;
//                if(one  != null)
//                    System.out.println("one begin_ip:" + one.beginIp + "; end_ip:" + one.endIp);
//                System.out.println("two begin_ip:" + two.beginIp + "; end_ip:" + two.endIp);
//                if(three  != null)
//                    System.out.println("three begin_ip:" + three.beginIp + "; end_ip:" + three.endIp);
            }else if(current.nBeginIp <= last.nEndIp){//外包含，切割成3
//                log.info("外包含");
//                log.info("last begin_ip:" + last.beginIp + "; end_ip:" + last.endIp);
//                log.info("current begin_ip:" + current.beginIp + "; end_ip:" + current.endIp);

                Location one = null;
                Location two = null;
                Location three = null;
                if(current.nBeginIp != last.nBeginIp){
                    one = new Location(last);
                    one.nEndIp = current.nBeginIp - 1;
                    one.endIp = Util.longToIP(one.nEndIp);
                }

                two = new Location(current);
                two.nEndIp = last.nEndIp;
                two.endIp = Util.longToIP(two.nEndIp);

                three = new Location(current);
                three.nBeginIp = last.nEndIp + 1;
                three.beginIp = Util.longToIP(three.nBeginIp);
                if(one != null){
                    count++;
                    load(one);
                    System.out.println("plus one");
                }

                ordLocations.add(three);
                if(last.country.equals("中国") || current.country.equals("中国")){
                    System.out.println(last.country + "," + last.province + ":" + last.beginIp + "\\" + last.endIp + "--"
                            + current.country + "," + current.province + ":" + current.beginIp + "\\" + current.endIp
                            + "==> " + (one == null? "": (one.beginIp + "\\" + one.endIp))  + "," + (two.beginIp + "\\" + two.endIp) + ","
                            + (three == null ? "" : (three.beginIp + "\\" + three.endIp)));
                }
                if(!last.country.equals(current.country)){
                    System.out.println("ERROR:" + last.country + "," + last.province + ":" + last.beginIp + "\\" + last.endIp + "--"
                            + current.country + "," + current.province + ":" + current.beginIp + "\\" + current.endIp);
                }
                last = two;
//                log.info("one begin_ip:" + one.beginIp + "; end_ip:" + one.endIp);
//                log.info("two begin_ip:" + two.beginIp + "; end_ip:" + two.endIp);
//                log.info("three begin_ip:" + three.beginIp + "; end_ip:" + three.endIp);
            }else{
                load(last);
                last = current;
            }
        }
        if(last != null)
            load(last);
        if(!ordLocations.isEmpty())
            System.out.println("is not empty");
        System.out.println("new count " + count);
    }

    static String sqlHead = "insert into cdn_clear (begin_ip,end_ip,country,area,province,city,county,isp,other,nbegin_ip,nend_ip) ";

    int insertNum = 0;
    void load(Location ip) throws SQLException {
        if(ip == null)
            return;
        //System.out.println("ip begin_ip:" + ip.beginIp + "; end_ip:" + ip.endIp);
        insertNum++;
        PreparedStatement statement = conn.prepareStatement(sqlHead + ip.getValueSql());
        statement.executeUpdate();
        statement.close();
    }

    static class TestRs implements ResultSet{
        public List<Map<String,String>> data = new ArrayList<Map<String, String>>();
        Map<String,String> current = null;
        @Override
        public boolean next() throws SQLException {
            if(data.isEmpty())
                return false;
            current = data.get(0);
            data.remove(0);
            return true;
        }

        @Override
        public void close() throws SQLException {

        }

        @Override
        public boolean wasNull() throws SQLException {
            return false;
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public boolean getBoolean(int columnIndex) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(int columnIndex) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(int columnIndex) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            return current.get(columnLabel);
        }

        @Override
        public boolean getBoolean(String columnLabel) throws SQLException {
            return false;
        }

        @Override
        public byte getByte(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public short getShort(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public long getLong(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public float getFloat(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public double getDouble(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
            return null;
        }

        @Override
        public byte[] getBytes(String columnLabel) throws SQLException {
            return new byte[0];
        }

        @Override
        public Date getDate(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getAsciiStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getUnicodeStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public InputStream getBinaryStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {

        }

        @Override
        public String getCursorName() throws SQLException {
            return null;
        }

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public int findColumn(String columnLabel) throws SQLException {
            return 0;
        }

        @Override
        public Reader getCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public boolean isBeforeFirst() throws SQLException {
            return false;
        }

        @Override
        public boolean isAfterLast() throws SQLException {
            return false;
        }

        @Override
        public boolean isFirst() throws SQLException {
            return false;
        }

        @Override
        public boolean isLast() throws SQLException {
            return false;
        }

        @Override
        public void beforeFirst() throws SQLException {

        }

        @Override
        public void afterLast() throws SQLException {

        }

        @Override
        public boolean first() throws SQLException {
            return false;
        }

        @Override
        public boolean last() throws SQLException {
            return false;
        }

        @Override
        public int getRow() throws SQLException {
            return 0;
        }

        @Override
        public boolean absolute(int row) throws SQLException {
            return false;
        }

        @Override
        public boolean relative(int rows) throws SQLException {
            return false;
        }

        @Override
        public boolean previous() throws SQLException {
            return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {

        }

        @Override
        public int getFetchDirection() throws SQLException {
            return 0;
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {

        }

        @Override
        public int getFetchSize() throws SQLException {
            return 0;
        }

        @Override
        public int getType() throws SQLException {
            return 0;
        }

        @Override
        public int getConcurrency() throws SQLException {
            return 0;
        }

        @Override
        public boolean rowUpdated() throws SQLException {
            return false;
        }

        @Override
        public boolean rowInserted() throws SQLException {
            return false;
        }

        @Override
        public boolean rowDeleted() throws SQLException {
            return false;
        }

        @Override
        public void updateNull(int columnIndex) throws SQLException {

        }

        @Override
        public void updateBoolean(int columnIndex, boolean x) throws SQLException {

        }

        @Override
        public void updateByte(int columnIndex, byte x) throws SQLException {

        }

        @Override
        public void updateShort(int columnIndex, short x) throws SQLException {

        }

        @Override
        public void updateInt(int columnIndex, int x) throws SQLException {

        }

        @Override
        public void updateLong(int columnIndex, long x) throws SQLException {

        }

        @Override
        public void updateFloat(int columnIndex, float x) throws SQLException {

        }

        @Override
        public void updateDouble(int columnIndex, double x) throws SQLException {

        }

        @Override
        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

        }

        @Override
        public void updateString(int columnIndex, String x) throws SQLException {

        }

        @Override
        public void updateBytes(int columnIndex, byte[] x) throws SQLException {

        }

        @Override
        public void updateDate(int columnIndex, Date x) throws SQLException {

        }

        @Override
        public void updateTime(int columnIndex, Time x) throws SQLException {

        }

        @Override
        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

        }

        @Override
        public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

        }

        @Override
        public void updateObject(int columnIndex, Object x) throws SQLException {

        }

        @Override
        public void updateNull(String columnLabel) throws SQLException {

        }

        @Override
        public void updateBoolean(String columnLabel, boolean x) throws SQLException {

        }

        @Override
        public void updateByte(String columnLabel, byte x) throws SQLException {

        }

        @Override
        public void updateShort(String columnLabel, short x) throws SQLException {

        }

        @Override
        public void updateInt(String columnLabel, int x) throws SQLException {

        }

        @Override
        public void updateLong(String columnLabel, long x) throws SQLException {

        }

        @Override
        public void updateFloat(String columnLabel, float x) throws SQLException {

        }

        @Override
        public void updateDouble(String columnLabel, double x) throws SQLException {

        }

        @Override
        public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

        }

        @Override
        public void updateString(String columnLabel, String x) throws SQLException {

        }

        @Override
        public void updateBytes(String columnLabel, byte[] x) throws SQLException {

        }

        @Override
        public void updateDate(String columnLabel, Date x) throws SQLException {

        }

        @Override
        public void updateTime(String columnLabel, Time x) throws SQLException {

        }

        @Override
        public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

        }

        @Override
        public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

        }

        @Override
        public void updateObject(String columnLabel, Object x) throws SQLException {

        }

        @Override
        public void insertRow() throws SQLException {

        }

        @Override
        public void updateRow() throws SQLException {

        }

        @Override
        public void deleteRow() throws SQLException {

        }

        @Override
        public void refreshRow() throws SQLException {

        }

        @Override
        public void cancelRowUpdates() throws SQLException {

        }

        @Override
        public void moveToInsertRow() throws SQLException {

        }

        @Override
        public void moveToCurrentRow() throws SQLException {

        }

        @Override
        public Statement getStatement() throws SQLException {
            return null;
        }

        @Override
        public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
            return null;
        }

        @Override
        public Ref getRef(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Blob getBlob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Clob getClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Array getArray(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Date getDate(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Time getTime(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public URL getURL(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateRef(int columnIndex, Ref x) throws SQLException {

        }

        @Override
        public void updateRef(String columnLabel, Ref x) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, Blob x) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, Blob x) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Clob x) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Clob x) throws SQLException {

        }

        @Override
        public void updateArray(int columnIndex, Array x) throws SQLException {

        }

        @Override
        public void updateArray(String columnLabel, Array x) throws SQLException {

        }

        @Override
        public RowId getRowId(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public RowId getRowId(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateRowId(int columnIndex, RowId x) throws SQLException {

        }

        @Override
        public void updateRowId(String columnLabel, RowId x) throws SQLException {

        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public void updateNString(int columnIndex, String nString) throws SQLException {

        }

        @Override
        public void updateNString(String columnLabel, String nString) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

        }

        @Override
        public NClob getNClob(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public NClob getNClob(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public SQLXML getSQLXML(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

        }

        @Override
        public String getNString(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public String getNString(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public Reader getNCharacterStream(int columnIndex) throws SQLException {
            return null;
        }

        @Override
        public Reader getNCharacterStream(String columnLabel) throws SQLException {
            return null;
        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

        }

        @Override
        public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

        }

        @Override
        public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

        }

        @Override
        public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

        }

        @Override
        public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

        }

        @Override
        public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

        }

        @Override
        public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

        }

        @Override
        public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

        }

        @Override
        public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

        }

        @Override
        public void updateClob(int columnIndex, Reader reader) throws SQLException {

        }

        @Override
        public void updateClob(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public void updateNClob(int columnIndex, Reader reader) throws SQLException {

        }

        @Override
        public void updateNClob(String columnLabel, Reader reader) throws SQLException {

        }

        @Override
        public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }

    public static void main(String[] agrs) throws SQLException {
        IpClear ipClear = new IpClear();
        Config conf = new Config();
        conf.username = "wfp";
        conf.passwd = "wfp123456";
        conf.mysqlUrl = "jdbc:mysql://192.168.1.12:3306/test_wfp";
//
//        TestRs testRs = new TestRs();
//        Map t1 = new HashMap();
//        t1.put("begin_ip")
//        testRs.data.add();
        ipClear.init(conf);
        ipClear.extract();
        ipClear.transfer();
        System.out.println("insert num " + ipClear.insertNum);
    }
}
