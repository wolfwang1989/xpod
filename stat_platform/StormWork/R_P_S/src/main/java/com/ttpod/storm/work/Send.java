package com.ttpod.storm.work;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 14-6-17.
 */
public  class Send implements Serializable {
    public List<String> streamid;
    public List<String> outputfield;
}
