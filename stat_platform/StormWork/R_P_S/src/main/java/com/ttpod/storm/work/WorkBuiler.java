package com.ttpod.storm.work;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.*;
import backtype.storm.tuple.Fields;
import com.ttpod.Record.Record;
import org.dom4j.DocumentException;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 14-6-17.
 */
public class WorkBuiler {
    static private Logger log = org.slf4j.LoggerFactory.getLogger(WorkBuiler.class);

    static final String FILE_PATH = "./dbusSpout.xml";

    static public StormTopology buildTopology(Config conf) {
        TopologyBuilder builder = new TopologyBuilder();

        ComponentInfo info = (ComponentInfo) conf.get(ConfigReader.STREAM_CONF_KEY);
        for(SpoutInfo spout: info.spout){
            Object spoutObject = null;
            try {
                spoutObject = Class.forName(spout.classname).newInstance();
            } catch (InstantiationException e) {
                log.error("",e);
                return null;
            } catch (IllegalAccessException e) {
                log.error("",e);
                return null;
            } catch (ClassNotFoundException e) {
                log.error("class not find:" + spout.classname ,e);
                return null;
            }
            SpoutDeclarer delare = builder.setSpout(spout.componentid, (IRichSpout) spoutObject,spout.excutornum);
            delare.setNumTasks(spout.tasknum);
        }

        for(BoltInfo bolt:info.bolt){
            Object boltObject = null;
            try {
                boltObject = Class.forName(bolt.classname).newInstance();
            } catch (InstantiationException e) {
                log.error("",e);
                return null;
            } catch (IllegalAccessException e) {
                log.error("",e);
                return null;
            } catch (ClassNotFoundException e) {
                log.error("class not find:" + bolt.classname ,e);
                return null;
            }
            BoltDeclarer delare = null;
            if(boltObject instanceof IBasicBolt)
                delare = builder.setBolt(bolt.componentid, (IBasicBolt) boltObject,bolt.excutornum);
            else if(boltObject instanceof IRichBolt)
                delare = builder.setBolt(bolt.componentid, (IRichBolt) boltObject,bolt.excutornum);
            else{
                log.error("bolt must be IBasicBolt or IRichBolt; ->" + bolt.classname);
                return null;
            }
            delare.setNumTasks(bolt.tasknum);
            if(bolt.recievers != null){
                for(Reciever reciever : bolt.recievers){
                    try {
                        Method m = null;
                        boolean isHashStreamId = reciever.streamid != null && reciever.streamid.length() > 0;
                        if(reciever.grouping_type.equals("fieldsGrouping")){
                            if(isHashStreamId == true){
                                m = delare.getClass().getMethod(reciever.grouping_type,String.class,String.class,Fields.class);
                                m.invoke(delare,reciever.from_componentid,reciever.streamid, new Fields(reciever.field));
                            }
                            else{
                                m = delare.getClass().getMethod(reciever.grouping_type,String.class,Fields.class);
                                m.invoke(delare,reciever.from_componentid, new Fields(reciever.field));
                            }
                        }
                        else{
                            if(isHashStreamId == true){
                                m = delare.getClass().getMethod(reciever.grouping_type,String.class,String.class);
                                m.invoke(delare,reciever.from_componentid, reciever.streamid);
                            }
                            else{
                                m = delare.getClass().getMethod(reciever.grouping_type,String.class);
                                m.invoke(delare,reciever.from_componentid);
                            }
                        }

                    } catch (NoSuchMethodException e) {
                        log.error("your grouping is not find",e);
                        return null;
                    } catch (InvocationTargetException e) {
                        log.error("",e);
                        return null;
                    } catch (IllegalAccessException e) {
                        log.error("",e);
                        return null;
                    }
                }
            }
        }

        return builder.createTopology();
    }

    static public void main(String[] args) throws DocumentException {
        Config config = ConfigReader.createStormConfig(FILE_PATH);
//        config.registerSerialization(Record.class);
        if(config == null){
            log.error("create conf failed");
            return;
        }
        StormTopology work = buildTopology(config);
        if(work == null){
            log.error("build storm job failed");
            return;
        }
        config.remove(ConfigReader.STREAM_CONF_KEY);
        if(args != null && args.length > 0){
            String name = args[0];
            try {
                StormSubmitter.submitTopology(name, config, work);
            } catch (AlreadyAliveException e) {
                log.error("please choose another job name,which is already ocupied",e);
            } catch (InvalidTopologyException e) {
                log.error("Topology is invalid , may be the config problem",e);
            }
        }else{
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("kafka", config, work);
        }
        return;
    }
}
