package com.ttpod.storm.work;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 14-6-17.
 */
public  class Reciever implements Serializable {
    public String grouping_type;
    public List<String> field;
    public String streamid;
    public String from_componentid;
}

