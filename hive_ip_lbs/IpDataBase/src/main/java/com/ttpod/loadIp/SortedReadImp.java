package com.ttpod.loadIp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Administrator on 14-5-15.
 */
public class SortedReadImp implements ReadInterface {
    TreeMap<Long,Location> sortedMap = new TreeMap<Long,Location>();
    ReadImp read ;
    Set<Map.Entry<Long,Location>> a;
    Iterator<Map.Entry<Long, Location>> b;
    @Override
    public boolean init(Config conf) {
        read = new ReadImp();
        if(!read.init(conf))
            return false;
        while(read.hasNext()){
            Location loc = read.read();
            if(loc != null)
                sortedMap.put(loc.nBeginIp,loc);
        }
        try {
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        a = sortedMap.entrySet();
        b = a.iterator();
        return true;
    }

    @Override
    public Location read() {
        return b.next().getValue();

    }

    @Override
    public boolean hasNext() {
        return b.hasNext();
    }

    @Override
    public void close() throws IOException {

    }
}
