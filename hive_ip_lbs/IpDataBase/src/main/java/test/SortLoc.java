package test;

import com.ttpod.loadIp.Location;
import com.ttpod.loadIp.Util;

import java.util.*;

/**
 * Created by Administrator on 14-6-27.
 */
public class SortLoc {
    static List<Location> ordLocations = new ArrayList<Location>();

    static class LocComprator implements Comparator<Location>{

        @Override
        public int compare(Location o1, Location o2) {
            if(o1.nBeginIp < o2.nBeginIp)
                return -1;
            else if(o1.nBeginIp == o2.nBeginIp)
                return 0;
            else
                return 1;
        }
    }
    public static void main(String[] args){
        System.out.println(Util.ipToLong("222.161.82.242"));
    }
}
