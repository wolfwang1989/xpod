package com.ttpod.storm.work;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 14-6-17.
 */
public class ComponentInfo implements Serializable{
    static Logger log = LoggerFactory.getLogger(ComponentInfo.class);

    public List<SpoutInfo> spout;
    public List<BoltInfo> bolt;
    Map<String,Object> idToCominfo;

    public boolean buildIndex(){
        if(spout == null || spout.size() == 0){
            log.error("please config spout,there is no any spout");
            return false;
        }
        if(bolt == null || bolt.size() == 0){


            log.error("please config bolt,there is no any bolt");
            return false;
        }
        idToCominfo = new HashMap<String, Object>();
        for(SpoutInfo sp : spout){
            idToCominfo.put(sp.componentid,sp);
        }
        for(BoltInfo bo : bolt){
            idToCominfo.put(bo.componentid,bo);
        }
        return true;
    }
}
