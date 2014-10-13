package com.ttpod.stat.hive.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 14-8-30.
 */
public class FindSongId extends UDF{
    public static final Set<String> SONG_IDS;
    static {
        SONG_IDS = new HashSet<String>();
//        SONG_IDS.add("307363");
//        SONG_IDS.add("1027732");
        SONG_IDS.add("27821624");
        SONG_IDS.add("27821622");
        SONG_IDS.add("27821621");
        SONG_IDS.add("27821608");
        SONG_IDS.add("27821607");
        SONG_IDS.add("27821606");
        SONG_IDS.add("27821605");
        SONG_IDS.add("27821604");
        SONG_IDS.add("27821595");
        SONG_IDS.add("27821594");
        SONG_IDS.add("27821593");
        SONG_IDS.add("27821592");
        SONG_IDS.add("27821591");
        SONG_IDS.add("27712629");
        SONG_IDS.add("27712626");
        SONG_IDS.add("27712625");
        SONG_IDS.add("27712624");
        SONG_IDS.add("27712623");
        SONG_IDS.add("27712622");
        SONG_IDS.add("27712621");
        SONG_IDS.add("27712620");
        SONG_IDS.add("27712619");
        SONG_IDS.add("27712618");
        SONG_IDS.add("27712616");
        SONG_IDS.add("27712615");
        SONG_IDS.add("27712614");
        SONG_IDS.add("27712612");
        SONG_IDS.add("27712611");
        SONG_IDS.add("27712610");
        SONG_IDS.add("27712608");
        SONG_IDS.add("27712607");
        SONG_IDS.add("27712606");
        SONG_IDS.add("27712604");
        SONG_IDS.add("27468089");
        SONG_IDS.add("27467080");
        SONG_IDS.add("27465179");
        SONG_IDS.add("27464046");
        SONG_IDS.add("27462079");
        SONG_IDS.add("27448950");
        SONG_IDS.add("27447240");
        SONG_IDS.add("27447135");
        SONG_IDS.add("27447106");
        SONG_IDS.add("27447075");
        SONG_IDS.add("27447059");
        SONG_IDS.add("27447047");
        SONG_IDS.add("27447038");
        SONG_IDS.add("27447020");
        SONG_IDS.add("27447001");
        SONG_IDS.add("27414988");
        SONG_IDS.add("27414987");
        SONG_IDS.add("27414986");
        SONG_IDS.add("27414985");
        SONG_IDS.add("27414984");
        SONG_IDS.add("27414983");
        SONG_IDS.add("27414981");
        SONG_IDS.add("27414977");
        SONG_IDS.add("27414971");
        SONG_IDS.add("27381848");
        SONG_IDS.add("27355424");
        SONG_IDS.add("27346166");
        SONG_IDS.add("27346165");
        SONG_IDS.add("27346164");
        SONG_IDS.add("27346163");
        SONG_IDS.add("27346162");
        SONG_IDS.add("27346161");
        SONG_IDS.add("27346160");
        SONG_IDS.add("27346159");
        SONG_IDS.add("27346157");
        SONG_IDS.add("27300418");
        SONG_IDS.add("27300416");
        SONG_IDS.add("27300411");
        SONG_IDS.add("27300410");
        SONG_IDS.add("27300408");
        SONG_IDS.add("27300407");
        SONG_IDS.add("27300397");
        SONG_IDS.add("27300360");
        SONG_IDS.add("27300351");
        SONG_IDS.add("27300337");
    }
    public FindSongId(){

    }

    public int evaluate(String songId){
        return  SONG_IDS.contains(songId) ? 1 : 0 ;
    }


}
