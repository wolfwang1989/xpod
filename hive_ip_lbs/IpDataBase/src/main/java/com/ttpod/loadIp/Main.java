package com.ttpod.loadIp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by wolf on 14-5-11.
 */
public class Main {
    static Log log = LogFactory.getLog(Main.class);
    static InsertInterface getInsert(Config conf){
        if(conf.mode.equals("add") || conf.mode.equals("mysql")){
            return  new MergeInsertImp();
        }else if(conf.mode.equals("all")){
            return new InsertImp();
        }
        return null;
    }
    static ReadInterface getRead(Config conf){
        if(conf.mode.equals("add")){
            return  new ReadImp();
        }else if(conf.mode.equals("all")){
            return new ReadImp();
        }
        else if(conf.mode.equals("mysql")){
            return new TableReadImp();
        }
        return null;
    }
    public  static void  main(String[] args) throws IOException, SQLException{
        Config conf = new Config(args);

        ReadInterface readInterface =  getRead(conf);
        if(readInterface == null){
            log.error("your mode isn't correct,get readimp failed");
            return;
        }
        if(! readInterface.init(conf)){
        	log.error("init read failed");
        	return;
        }else{
            log.info("init read success");
        }
        InsertInterface insertInterface = getInsert(conf);
        if(insertInterface == null){
            log.error("your mode isn't correct,get insert failed");
            return;
        }
        if(!insertInterface.init(conf)){
            log.error("init insert failed");
            return;
        }else{
            log.info("init insert success");
        }
//        if(conf.mode.equals("add")){
//            long last = 0;
//            while(readInterface.hasNext()){
//                Location loc = readInterface.read();
//                if(loc == null)
//                    continue;
//                if(loc.nBeginIp <= last || loc.nBeginIp > loc.nBeginIp){
//                    log.error("error loc:" + loc.toString());
//                    continue;
//                }
//                else {
//                    last = loc.nEndIp;
//                }
//
//                insertInterface.Insert(loc);
//            }
//        }
//        else{
            while(readInterface.hasNext()){
                Location loc = readInterface.read();

                if(loc != null)
                    insertInterface.Insert(loc);
            }
//        }
        readInterface.close();
        insertInterface.close();
    }


}
