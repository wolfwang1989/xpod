package com.ttpod.storm.ipstat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Administrator on 14-6-18.
 */
public class RedisOp implements Runnable {
    static Logger log = LoggerFactory.getLogger(RedisOp.class);


    String _host;
    Jedis _jedis;
    volatile ConcurrentHashMap<String,Set<String>> _data;
    ConcurrentSkipListSet<String> def_set = new ConcurrentSkipListSet<String>();

    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public RedisOp(String host){
        _host = host;
        _jedis = new Jedis(host);
        _data = new ConcurrentHashMap<String, Set<String>>();
        new Thread(this).start();
    }

    boolean addStat(String key,String val){
        readWriteLock.readLock().lock();
        Set v = _data.get(key);
        if(v == null){
            ConcurrentSkipListSet<String> tmpSet = new ConcurrentSkipListSet<String>();
            v = _data.putIfAbsent(key,tmpSet);
            if(v != null)
                v.add(val);
            else
                tmpSet.add(val);
        }else{
            v.add(val);
        }
        readWriteLock.readLock().unlock();
        return true;
    }
    @Override
    public void run() {
        while(true){
            readWriteLock.writeLock().lock();
            ConcurrentHashMap<String,Set<String> > toWrite = _data;
            _data = new ConcurrentHashMap<String, Set<String>>();
            readWriteLock.writeLock().unlock();
            for(Map.Entry<String,Set<String>> entry:toWrite.entrySet()){
                for(String uid : entry.getValue()){
                    try{
                        _jedis.sadd(entry.getKey(),uid);
                    }
                    catch (Exception e){
                        log.error("commit to redis failed" ,e);
                    }
                }
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.error("",e);
            }
        }
    }
}
