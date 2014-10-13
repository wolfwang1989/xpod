package com.ttpod.storm.ipstat;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.ttpod.Record.ILog;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 14-6-18.
 */
public class IpStatBolt extends BaseBasicBolt{

    static Logger log = LoggerFactory.getLogger(IpStatBolt.class);
    static Reader reader;
    static RedisOp redisOp;
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);

        synchronized (log){
            if(reader == null){
                try {
//                    List<Long> db =  (List <Long>)stormConf.get("com.ttpod.ip.db");
//                    byte[]  buf = new byte[db.size()];
//                    for(int i = 0;i < db.size();++i){
//                        buf[i] = (byte) (0xFF & db.get(i));
//                    }
                    log.info(String.valueOf(this.getClass().getClassLoader().getResource("my_ip.ttdb")));
                    InputStream inputStream = ClassLoader.getSystemResourceAsStream("my_ip.ttdb");
                    ThreadBuffer threadBuffer = new ThreadBuffer(inputStream);
                    inputStream.close();
                    reader = new Reader(threadBuffer, "IpStatBolt");
                } catch (IOException e) {
                    log.error("",e);
                    System.exit(0);
                }
                catch (Exception e){
                    log.error("",e);
                    System.exit(0);
                }
            }
            if(redisOp == null){
                try{
                    redisOp = new RedisOp((String) stormConf.get("com.ttpod.redis.host"));
                }catch (Exception e){
                    log.error("init Jedis failed",e);
                    System.exit(0);
                }
            }
        }
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        ILog record = (ILog) input.getValue(0);
        Location loc = null;
        try {
            loc = reader.get(record.getString("remote_addr"));
        } catch (IOException e) {
            log.error("read ip address failed,ip = " + record.getString("remote_addr"));
            return;
        }
        if(loc == null  || loc.country == null || loc.country.length() == 0)
            return;
        String uuid = record.getString("request_body.param.uid");
        if(uuid == null){
            log.error("uuid is null");
            return;
        }
        Long logTime = null;
        DateTime time = null;
        try{
            logTime = record.getLong("request_body.time");
            if(logTime == null){
                log.error("time is null");
                return;
            }
            time = new DateTime(logTime);
        }
        catch (ClassCastException e){
            log.error("request_body.time cast error",e);
            return;
        }
        catch (Exception e){
            log.error("",e);
            return;
        }


//        DateTime time = null;
//        if(logTime instanceof Long)
//            time = new DateTime(logTime);
//        else if(logTime instanceof Integer)
//            time = new DateTime(1000 * ((Integer)logTime));
//        else{
//            log.error("time isn't int or long");
//            return;
//        }
        String dayKey = new StringBuilder(20)
                                .append(time.getYear())
                                .append("_")
                                .append(time.getMonthOfYear())
                                .append("_")
                                .append(time.getDayOfMonth())
                                .append("_")
                                .append(loc.country)
                                .append("_")
                                .append(loc.province)
                                .toString();
        String hourKey =  new StringBuilder(20)
                .append(time.getYear())
                .append("_")
                .append(time.getMonthOfYear())
                .append("_")
                .append(time.getDayOfMonth())
                .append("_")
                .append(time.getHourOfDay())
                .append("_")
                .append(loc.country)
                .append("_")
                .append(loc.province)
                .toString();

        redisOp.addStat(dayKey,uuid);
        redisOp.addStat(hourKey,uuid);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
