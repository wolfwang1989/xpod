package com.ttpod.stat.hive.udf;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * Created by Administrator on 14-8-30.
 */
public class FindSongIdNum extends UDF {
    public FindSongIdNum(){

    }

    public int evaluate(double songId){
        return  FindSongId.SONG_IDS.contains(Long.toString(new Double(songId).longValue())) ? 1 : 0 ;
    }
}
