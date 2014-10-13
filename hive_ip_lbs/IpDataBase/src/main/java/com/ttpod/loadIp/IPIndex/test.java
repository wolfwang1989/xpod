package com.ttpod.loadIp.IPIndex;

import com.ttpod.loadIp.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-5-12.
 */
public class test {

    public static class SubNet{
        String startIp;
        long   netMark;
        SubNet(long ip,long mark){
            startIp  = Util.longToIP(ip);
            netMark = mark;
        }

        @Override
        public String toString() {
            return startIp + "/" + Integer.toString((int) netMark);
        }
    }
    static  final long one = 0x01;
    static  long getRangeWithIndex(long i){
        return (one << i) - 1;
    }
    static  long getBitValWithIndex(long val,long i){
        return (val >> (i-1) ) & one;
    }

    public static List<SubNet> createSub(String start,String end){
        List<SubNet> ret = new ArrayList<SubNet>();
        long nSatrt = Util.ipToLong(start);
        long nEnd = Util.ipToLong(end);
        long d = nEnd - nSatrt;
        if(d < 0)
            return ret;
        for(long i = 1;i <= 32;++i){
            long t = getBitValWithIndex(nSatrt,i);
            long range =getRangeWithIndex(i);
            if(range < d ){
                if(t == 0){
                    continue;
                }
                else{
                    long lastI = i - 1;
                    ret.add(new SubNet(nSatrt,32 - lastI ));
                    nSatrt += getRangeWithIndex(lastI) + 1 ;
                    d = nEnd - nSatrt;
                    i = 0;
                }
            }
            else if(range == d){
                if(t == 0){
                    ret.add(new SubNet(nSatrt,32 - i ));
                    break;
                }
                else{
                    long lastI = i - 1;
                    ret.add(new SubNet(nSatrt,32 - lastI ));
                    nSatrt += getRangeWithIndex(lastI) + 1 ;
                    d = nEnd - nSatrt;
                    i = 0;
                }
            }
            else{

                long lastI = i - 1;
                ret.add(new SubNet(nSatrt,32 - lastI ));
                nSatrt += getRangeWithIndex(lastI) + 1;
                d = nEnd - nSatrt;
                i = 0;
            }
            if(d < 0){
                break;
            }
        }

        return ret;
    }
    static public void main(String[] agrs) throws IOException {
        System.out.println(createSub("10.0.0.0","10.0.0.255"));
        System.out.println(createSub("10.0.0.0","10.0.0.1"));
        System.out.println(createSub("10.0.0.0","10.0.0.0"));
        System.out.println(createSub("0.0.0.0","0.255.255.255"));
        System.out.println(createSub("101.126.61.0","101.126.62.255"));
        System.out.println(createSub("101.224.0.0","101.224.0.12"));
        System.out.println(createSub("101.128.8.0","101.128.63.255"));
    }
}
