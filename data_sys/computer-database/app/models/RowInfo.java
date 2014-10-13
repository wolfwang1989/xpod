package models;

/**
 * Created by Administrator on 14-8-21.
 */
public class RowInfo {

    public String songId;
    public String songName;
    public String singerName;

    public RowInfo(String song_name,String singer_name,String song_id){
        songId = song_id;
        songName = song_name;
        singerName = singer_name;
    }

}
