package com.ttpod.loadIp;


import java.sql.SQLException;

/**
 * Created by wolf on 14-5-11.
 */
public interface InsertInterface {

    public  boolean init(Config conf);

    public  void Insert(Location ip);

    public  void close() throws SQLException;
}
