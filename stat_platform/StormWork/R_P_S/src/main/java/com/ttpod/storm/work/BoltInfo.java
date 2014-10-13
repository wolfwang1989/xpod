package com.ttpod.storm.work;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 14-6-17.
 */
public class BoltInfo implements Serializable{
    public String componentid;
    public String classname;
    public int tasknum = 1;
    public int excutornum  = 1;
    public List<Reciever> recievers;

}
