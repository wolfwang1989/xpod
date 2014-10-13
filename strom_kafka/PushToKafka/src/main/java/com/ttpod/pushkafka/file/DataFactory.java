package com.ttpod.pushkafka.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 14-6-23.
 */
public class DataFactory {
    private static BlockingQueue<String> data = null;

    static Logger log  = LoggerFactory.getLogger(DataFactory.class);

    static final int DEFAULT_QUESIZE = 10000;
    public static BlockingQueue<String> getDataQue(){
        return data;
    }

    public static boolean init(int num){
        synchronized (log){
            data = new ArrayBlockingQueue<String>(num);
        }
        return true;
    }

}
