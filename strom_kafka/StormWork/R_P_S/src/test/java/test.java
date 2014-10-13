import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.joda.time.DateTime;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 14-6-12.
 */
public class test {

    static String def_str = "{\"data\":[{\"optvalue2\":0,\"v\":1,\"time\":\"20140616104721\",\"module\":\"find_music\",\"optmessage\":\"96274896\",\"value\":1,\"origin\":\"category-华语新歌精选\",\"optmessage2\":\"华语新歌精选\",\"optvalue\":0,\"type\":\"listen\",\"key\":0},{\"optvalue2\":171896,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104700\",\"optmessage\":\"张国荣\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"optvalue2\":131662,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104649\",\"optmessage\":\"张国荣\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"optvalue2\":43662,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104648\",\"optmessage\":\"张国荣\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"optvalue2\":129075,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104648\",\"optmessage\":\"张国荣\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"optvalue2\":41442,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104639\",\"optmessage\":\"张国荣\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"optvalue2\":611897,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104634\",\"optmessage\":\"张国荣\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"optvalue2\":145486,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104631\",\"optmessage\":\"张国荣\",\"origin\":\"lyric\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"optvalue2\":611897,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104631\",\"optmessage\":\"张国荣\",\"origin\":\"lyric\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"无胆入情关\",\"key\":0},{\"httpheader_received_time2\":207,\"module\":\"song\",\"song_id\":4433690,\"is_buffer\":0,\"dnsdone_time2\":0,\"file_size\":8611152,\"buffer_time\":75393,\"buffer_size\":7271225,\"buffer_count\":0,\"httpheader_received_time\":66,\"postion\":0,\"origin\":\"local\",\"connetdone_time\":75,\"dnsdone_time\":269835193,\"type\":\"listen_info\",\"url\":\"http:\\/\\/ws.cs.hotchanson.com\\/mp3_190_44\\/37\\/92\\/374dbe53f6ce423b7ca4f8ce5ce51892.mp3?k=248b55a0be290fbc&t=1403318716\",\"v\":1,\"time\":1402886790677,\"response_time\":269835862,\"play_time\":75,\"loading_time\":0,\"connetdone_time2\":100,\"song_time\":358,\"key\":0,\"play_control\":1},{\"optvalue2\":0,\"v\":1,\"time\":\"20140616104629\",\"module\":\"find_music\",\"optmessage\":\"96274896\",\"value\":1,\"origin\":\"category-华语新歌精选\",\"optmessage2\":\"华语新歌精选\",\"optvalue\":0,\"type\":\"listen\",\"key\":0},{\"optvalue2\":44778,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104614\",\"optmessage\":\"游鸿明\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"optvalue2\":0,\"v\":1,\"time\":\"201406161045\",\"module\":\"error\",\"value\":1,\"origin\":\"song\",\"optvalue\":25253416,\"type\":\"not_url\",\"key\":0},{\"optvalue2\":0,\"v\":1,\"time\":\"201406161045\",\"module\":\"error\",\"value\":1,\"origin\":\"song\",\"optvalue\":25104677,\"type\":\"not_url\",\"key\":0},{\"optvalue2\":0,\"v\":1,\"time\":\"201406161045\",\"module\":\"error\",\"value\":1,\"origin\":\"song\",\"optvalue\":19065788,\"type\":\"not_url\",\"key\":0},{\"httpheader_received_time2\":0,\"module\":\"song\",\"song_id\":4433690,\"is_buffer\":0,\"dnsdone_time2\":2,\"file_size\":0,\"buffer_time\":269835188,\"buffer_size\":0,\"buffer_count\":0,\"httpheader_received_time\":96,\"postion\":243,\"origin\":\"local\",\"connetdone_time\":1053,\"dnsdone_time\":58,\"type\":\"listen_info\",\"v\":1,\"time\":1402886714609,\"response_time\":0,\"play_time\":0,\"loading_time\":0,\"connetdone_time2\":87,\"song_time\":0,\"key\":0,\"play_control\":1},{\"optvalue2\":502011,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104514\",\"optmessage\":\"游鸿明\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"optvalue2\":112330,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104513\",\"optmessage\":\"游鸿明\",\"origin\":\"lyric\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"optvalue2\":78227,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104512\",\"optmessage\":\"游鸿明\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"optvalue2\":42070,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104512\",\"optmessage\":\"游鸿明\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"position\":\"0\",\"module\":\"download\",\"down_file_size\":1949390,\"downstatus\":\"success\",\"origin\":\"local\",\"fileid\":\"1624145\",\"type\":\"song\",\"url\":\"http:\\/\\/hzd.exi.hotchaleur.com\\/mp3_64_16\\/66\\/70\\/662cf825ba4240df75d4487796e3ed70.mp3?k=e05123c549da7690&t=1403314075\",\"v\":1,\"filesize\":\"1949390\",\"response_time\":\"14011\",\"cutoff_count\":\"0\",\"download_time\":\"20969\",\"filename\":\"女人不是妖\",\"key\":0},{\"optvalue2\":45273,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104511\",\"optmessage\":\"游鸿明\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"optvalue2\":4433690,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104505\",\"optmessage\":\"游鸿明\",\"origin\":\"lyric\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"optvalue2\":4433690,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104501\",\"optmessage\":\"游鸿明\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"楼下那个女人\",\"key\":0},{\"httpheader_received_time2\":189,\"module\":\"song\",\"song_id\":1624145,\"is_buffer\":0,\"dnsdone_time2\":338,\"file_size\":5847696,\"buffer_time\":19888,\"buffer_size\":5847696,\"buffer_count\":0,\"httpheader_received_time\":182,\"postion\":229,\"origin\":\"local\",\"connetdone_time\":92,\"dnsdone_time\":71,\"type\":\"listen_info\",\"url\":\"http:\\/\\/hzd.exi.hotchaleur.com\\/mp3_190_16\\/98\\/26\\/980ecf6981d4e39245c14bb6d5b2ad26.mp3?k=4ad90a22e7ba565b&t=1403314075\",\"v\":1,\"time\":1402886700976,\"response_time\":1389,\"play_time\":56,\"loading_time\":0,\"connetdone_time2\":189,\"song_time\":243,\"key\":0,\"play_control\":1},{\"optvalue2\":0,\"v\":1,\"time\":\"20140616104500\",\"module\":\"find_music\",\"optmessage\":\"96274896\",\"value\":1,\"origin\":\"category-华语新歌精选\",\"optmessage2\":\"华语新歌精选\",\"optvalue\":0,\"type\":\"listen\",\"key\":0},{\"optvalue2\":229,\"v\":1,\"time\":\"20140616104450\",\"module\":\"find_music\",\"optmessage\":\"女人不是妖\",\"value\":1,\"origin\":\"local\",\"optmessage2\":\"52b881b2-ad29-492f-b4e3-20a8ae7897fb\",\"optvalue\":0,\"type\":\"menu\",\"key\":0},{\"optvalue2\":1622444,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104436\",\"optmessage\":\"司徒兰芳\",\"origin\":\"lyric\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"女人不是妖\",\"key\":0},{\"optvalue2\":72442,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104433\",\"optmessage\":\"司徒兰芳\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"女人不是妖\",\"key\":0},{\"position\":\"0\",\"module\":\"download\",\"down_file_size\":2667852,\"downstatus\":\"success\",\"origin\":\"local\",\"fileid\":\"1521253\",\"type\":\"song\",\"url\":\"http:\\/\\/hzd.exi.hotchaleur.com\\/mp3_64_15\\/39\\/45\\/397ede508524c968a7083a21a7bb6745.mp3?k=f5db6bde1ceb7e18&t=1403314075\",\"v\":1,\"filesize\":\"2667852\",\"response_time\":\"13314\",\"cutoff_count\":\"0\",\"download_time\":\"30692\",\"filename\":\"爱情没有那么美dj\",\"key\":0},{\"optvalue2\":72265,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104424\",\"optmessage\":\"司徒兰芳\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"女人不是妖\",\"key\":0},{\"optvalue2\":72470,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104424\",\"optmessage\":\"司徒兰芳\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"女人不是妖\",\"key\":0},{\"optvalue2\":72396,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104419\",\"optmessage\":\"司徒兰芳\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"女人不是妖\",\"key\":0},{\"optvalue2\":1624145,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104416\",\"optmessage\":\"司徒兰芳\",\"origin\":\"lyric\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"女人不是妖\",\"key\":0},{\"optvalue2\":1624145,\"v\":1,\"module\":\"lyric_pic\",\"time\":\"20140616104414\",\"optmessage\":\"司徒兰芳\",\"origin\":\"picture\",\"value\":1,\"type\":\"search\",\"optvalue\":1,\"optmessage2\":\"女人不是妖\",\"key\":0},{\"httpheader_received_time2\":738,\"module\":\"song\",\"song_id\":1521253,\"is_buffer\":0,\"dnsdone_time2\":305,\"file_size\":8005843,\"buffer_time\":38175,\"buffer_size\":8005843,\"buffer_count\":0,\"httpheader_received_time\":181,\"postion\":0,\"origin\":\"local\",\"connetdone_time\":1156,\"dnsdone_time\":269661483,\"type\":\"listen_info\",\"url\":\"http:\\/\\/hzd.exi.hotchaleur.com\\/mp3_128_15\\/99\\/42\\/99141ee6fc086e629951a7a615aa1542.mp3?k=b727cb68542166c5&t=1403318542\",\"v\":1,\"time\":1402886642728,\"response_time\":269664615,\"play_time\":98,\"loading_time\":0,\"connetdone_time2\":195,\"song_time\":333,\"key\":0,\"play_control\":1},{\"optvalue2\":0,\"v\":1,\"time\":\"20140616104402\",\"module\":\"find_music\",\"optmessage\":\"96274896\",\"value\":1,\"origin\":\"category-华语新歌精选\",\"optmessage2\":\"华语新歌精选\",\"optvalue\":0,\"type\":\"listen\",\"key\":0},{\"optvalue2\":228,\"v\":1,\"time\":\"20140616104357\",\"module\":\"find_music\",\"optmessage\":\"爱情没有那么美dj\",\"value\":1,\"origin\":\"local\",\"optmessage2\":\"50b835c8-0aa4-42aa-baf3-30c969022575\",\"optvalue\":0,\"type\":\"menu\",\"key\":0}],\"param\":{\"openudid\":\"a6cc2aeb5e897220\",\"uid\":\"864260025235867\",\"f\":\"f384\",\"app\":\"ttpod\",\"hid\":\"2726141439050120\",\"rom\":\"Xiaomi%2Fpisces%2Fpisces%3A4.2.1%2FJOP40D%2FJXCCNBD20.0%3Auser%2Frelease-keys\",\"net\":2,\"v\":\"v7.2.0.2014052910\",\"s\":\"s200\",\"active\":0,\"mid\":\"MI+3\",\"imsi\":\"460004910357659\",\"splus\":\"4.2.1%2F17\",\"tid\":0},\"time\":1402886841923,\"uuid\":\"deb1cee4-fe01-4a75-a36a-2fad17062078\"}";

    static void zktest() throws IOException, KeeperException, InterruptedException {
        String zkConnStr = "hadoop01:2181,hadoop02:2181,hadoop05:2181";
        List x = new ArrayList();
        for(int i = 0; i < 150;++i){
            ZooKeeper zk = new ZooKeeper(zkConnStr,10000,new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {

                }
            });
            zk.exists("/brokers",new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {

                }
            });
            x.add(zk);
        }
        System.out.println("init zk success");
    }

    static boolean souretest() throws IOException {
        String command2 = "tailf /data/logs/ttpod_client/access.log";
        Process process=Runtime.getRuntime().exec(command2);
        System.out.println("开始执行shell=="+command2);
        BufferedReader br = new BufferedReader(new InputStreamReader(process
                .getInputStream()));
        String line = new String();
        while ((line = br.readLine()) != null) {

        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("processes was interrupted");
            return false;
        }
        br.close();
        int ret = process.exitValue();
        System.out.println(ret);
        System.out.println("执行完毕!");
        return true;
    }
    public static void main(String[]args) throws IOException, InterruptedException, KeeperException {
        long a = System.currentTimeMillis();
        a = 1403056443123L;
//        a = 1403077920427L;
        System.out.println(a);
        DateTime time;
        time = new DateTime(a);
        System.out.println(time.getYear());
        System.out.println(time.getMonthOfYear());
        System.out.println(time.getDayOfMonth());
        Jedis jedis = new Jedis("hadoop02");
        Set<String> keys = jedis.keys("2014*");
        for(String key:keys){
            System.out.println(key + ":" + jedis.smembers(key));
        }
        zktest();
        while (true){
            Thread.sleep(1000);
        }
//        ObjectMapper m = new ObjectMapper();
//        Map x1 = new LinkedHashMap();
//
//        for(int i = 1;i < 1000;++i){
//            Map x2 = new LinkedHashMap();
//            x2.put("data",123);
//            x1.put("data" + Integer.toString(i),x2);
//        }
//
//        ByteArrayOutputStream b = new ByteArrayOutputStream();
//        m.writeValue(b ,x1);
//        String json = b.toString();
//        System.out.println(def_str);
//        Map jackson = null;
//        Map fastJson = null;
//
//        {
//            int n = 10000;
//            long start = System.currentTimeMillis();
//            while(n-- > 0){
//                fastJson = JSON.parseObject(def_str, HashMap.class);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("deser time cost fastJson = " + (end - start) +  " /ms");
//        }
//        {
//            int n = 10000;
//            long start = System.currentTimeMillis();
//            while(n-- > 0){
//                jackson = m.readValue(def_str, HashMap.class);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("deser time cost jackson = " + (end - start) +  " /ms");
//        }
//        Kryo kryo = new Kryo();
//
//        {
//            ByteArrayOutputStream b1 = new ByteArrayOutputStream();
//            Output output = new Output(b1);
//            kryo.writeObject(output, fastJson);
//            output.flush();
//            System.out.println("size fastJson = " + b1.toString().length() +  " #end");
//            int n = 10000;
//            long start = System.currentTimeMillis();
//            while(n-- > 0){
//                Map a = kryo.readObject(new Input(b1.toByteArray()),HashMap.class);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("time cost fastJson = " + (end - start) +  " /ms");
//        }
//        {
//            ByteArrayOutputStream b1 = new ByteArrayOutputStream();
//            Output output = new Output(b1);
//            kryo.writeObject(output, jackson);
//            output.flush();
//            System.out.println("size jackson = " + b1.toString().length() +  " #end");
//            int n = 10000;
//            long start = System.currentTimeMillis();
//            while(n-- > 0){
//                Map a = kryo.readObject(new Input(b1.toByteArray()),HashMap.class);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("time cost jackson = " + (end - start) +  " /ms");
//        }
//        {
//            ByteArrayOutputStream b1 = new ByteArrayOutputStream();
//            Output output = new Output(b1);
//            kryo.writeObject(output, x1);
//            output.flush();
//            System.out.println("size java_build_in = " + b1.toString().length() +  " #end");
//            int n = 10000;
//            long start = System.currentTimeMillis();
//            while(n-- > 0){
//                Map a = kryo.readObject(new Input(b1.toByteArray()),HashMap.class);
//            }
//            long end = System.currentTimeMillis();
//            System.out.println("time cost java_build_in = " + (end - start) +  " /ms");
//        }
    }
}
