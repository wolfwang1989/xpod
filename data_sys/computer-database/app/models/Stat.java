package models;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * Created by Administrator on 14-8-7.
 */
public class Stat {

    static public Configuration conf;
    static public HConnection connection;
    static {
        conf = new Configuration();
        conf.addResource(new Path("./conf/hbase_conf.xml"));
        conf.addResource(new Path("../conf/hbase_conf.xml"));
        try {
            connection = HConnectionManager.createConnection(conf);
        } catch (IOException e) {
            connection = null;
        }
    }

    public long  song_err = 0;
    public long  song_succ = 0;
    public long  song_kadun = 0;

    public float  errRate = 0;
    public float  kadunRate = 0;


    public long  download_succ = 0;
    public long  download_err = 0;
    public long  download_delete = 0;

    public float  down_errRate = 0;
    public float  down_delRate = 0;

    public final String rowkey;
    public final String version1;
    public Stat(String version,String date){
        rowkey = version + "_" + date;
        version1 = version;
    }

    public boolean init(){
        if(!getSongstat())
            return false;
        if(!getDownloadStat())
            return false;
        return true;
    }

    private  boolean getSongstat(){
        try {
            HTableInterface table = connection.getTable(conf.get("org.ttpod.stat.song.tablename").getBytes());
            Result row = table.get(new Get(rowkey.getBytes()));
            if(row == null){
                return false;
            }
            byte[] bytes = row.getValue("stat".getBytes(),"stattype_0_count".getBytes());
            song_succ = bytes != null ? Bytes.toLong(bytes) : 0;
            bytes = row.getValue("stat".getBytes(),"stattype_1_count".getBytes());
            song_kadun = bytes != null ? Bytes.toLong(bytes) : 0;
            bytes = row.getValue("stat".getBytes(),"stattype_2_count".getBytes());
            song_err = bytes != null ? Bytes.toLong(bytes) : 0;
            errRate = ((float) song_err) / (song_err + song_kadun + song_succ);
            kadunRate = ((float) song_kadun) / (song_err + song_kadun + song_succ);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean getDownloadStat(){
        try {
            HTableInterface table = connection.getTable(conf.get("org.ttpod.stat.download.tablename").getBytes());
            Result row = table.get(new Get(rowkey.getBytes()));
            if(row == null){
                return false;
            }
            byte[] bytes = row.getValue("stat".getBytes(),"stattype_0_count".getBytes());
            download_succ = bytes != null ? Bytes.toLong(bytes) : 0;
            bytes = row.getValue("stat".getBytes(),"stattype_1_count".getBytes());
            download_err = bytes != null ? Bytes.toLong(bytes) : 0;
            bytes = row.getValue("stat".getBytes(),"stattype_2_count".getBytes());
            download_delete = bytes != null ? Bytes.toLong(bytes) : 0;
            down_errRate = ((float) download_err) / (download_err + download_delete + download_succ);
            down_delRate = ((float) download_delete) / (download_err + download_delete + download_succ);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
