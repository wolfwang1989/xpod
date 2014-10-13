package com.ttpod.stat.hive.loadData;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.*;
import java.sql.SQLException;

/**
 * Created by Administrator on 14-5-9.
 */
public class testMqlConn {

 static Log log = LogFactory.getLog(testMqlConn.class);


 public static void main(String[] args) throws SQLException, UnknownHostException, SocketException {
     InetAddress localHost = Inet4Address.getLocalHost();
     NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);

     for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
         System.out.println(address.getAddress());
         System.out.println(address.getNetworkPrefixLength());

     }

 }
}
