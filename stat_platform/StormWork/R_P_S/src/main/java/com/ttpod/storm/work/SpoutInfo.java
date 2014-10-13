package com.ttpod.storm.work;

import java.io.Serializable;

/**
 * Created by Administrator on 14-6-17.
 */
public class SpoutInfo implements Serializable {
    public String componentid;
    public String classname;
    public int tasknum = 1;
    public int excutornum  = 1;
}
