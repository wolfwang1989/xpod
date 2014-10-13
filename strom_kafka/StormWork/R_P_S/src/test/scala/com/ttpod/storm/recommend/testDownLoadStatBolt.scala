package com.ttpod.storm.recommend

import org.scalatest.junit.JUnit3Suite
import com.ttpod.storm.stat.DownLoadStatBolt
import com.ttpod.storm.work.ConfigReader
import com.ttpod.Record.{Record, FieldInfoFactory}

/**
 * Created by Administrator on 14-8-18.
 */
class testDownLoadStatBolt extends JUnit3Suite{

  val urlTestLog = "183.207.178.237`53188`/ttpod_client/index.html`POST`[18/Aug/2014:14:36:51 +0800]`{\"data\":[{\"httpheader_received_time2\":0,\"module\":\"song\",\"song_id\":3657916,\"is_buffer\":0,\"dnsdone_time2\":2,\"file_size\":5677347,\"buffer_time\":9386,\"buffer_size\":0,\"buffer_count\":0,\"httpheader_received_time\":0,\"postion\":147,\"origin\":\"library-music-category_DJ舞曲_9d474442-7934-41b6-9a5e-68b006d31559\",\"connetdone_time\":751,\"dnsdone_time\":158,\"type\":\"listen_info\",\"url\":\"http:\\/\\/bav.kvb.yymommy.com\\/mp3_128_36\\/fa\\/b8\\/fa154671971ef6d3b17e2ccea753a6b8.mp3?k=6f68c8ff89c68367&t=1408775196\",\"v\":1,\"time\":1408343927599,\"response_time\":985,\"play_time\":354,\"loading_time\":0,\"connetdone_time2\":0,\"song_time\":354,\"key\":0,\"play_control\":1},{\"module\":\"song\",\"song_id\":3537336,\"is_buffer\":0,\"file_size\":0,\"buffer_time\":11973248,\"buffer_size\":0,\"buffer_count\":0,\"httpheader_received_time\":0,\"postion\":148,\"origin\":\"library-music-category_DJ舞曲_9d474442-7934-41b6-9a5e-68b006d31559\",\"connetdone_time\":0,\"dnsdone_time\":212,\"type\":\"listen_info\",\"v\":1,\"time\":1408343571271,\"response_time\":0,\"play_time\":0,\"loading_time\":0,\"song_time\":0,\"key\":0,\"play_control\":1}],\"param\":{\"openudid\":\"598222f0179b7259\",\"uid\":\"358281050386519\",\"f\":\"f198\",\"app\":\"ttpod\",\"hid\":\"\",\"rom\":\"htc%2Fhtccn_chs_cu%2Fg3u%3A4.1.2%2FJZO54K%2F251763.2%3Auser%2Frelease-keys\",\"net\":2,\"v\":\"v7.3.1.2014072413\",\"s\":\"s200\",\"active\":0,\"mid\":\"HTC+301e\",\"imsi\":\"460015156507074\",\"splus\":\"4.1.2%2F16\",\"tid\":196857047},\"time\":1408343927605,\"uuid\":\"15ec90e8-8474-4263-b1f9-83e5ffec6f8c\"}\n112.96.30.140`48592`/ttpod_client/index.html`POST`[18/Aug/2014:14:36:51 +0800]`{\"data\":[{\"optvalue2\":0,\"v\":1,\"time\":\"201408181438\",\"module\":\"startup\",\"value\":1,\"origin\":\"startup\",\"optvalue\":1,\"type\":\"startup\"},{\"optvalue2\":0,\"v\":1,\"time\":\"201408181437\",\"module\":\"audio_effect\",\"value\":1,\"origin\":\"audio-effect-best\",\"optvalue\":0,\"type\":\"show\"}],\"param\":{\"uid\":\"869005010210697\",\"f\":\"f384\",\"v\":\"v6.6.1.2014012410\",\"app\":\"ttpod\",\"hid\":\"\",\"s\":\"s200\",\"rom\":\"unknown\",\"active\":0,\"mid\":\"A4G\",\"imsi\":\"460010415221862\",\"splus\":\"4.1.1%2F15\",\"tid\":0,\"net\":1},\"time\":1408343916922,\"uuid\":\"fc9d727c-8e15-4540-8a1e-b231b00a58c5\"}"
  val urlTestLog2 = "183.207.178.237`53188`/ttpod_cli/ent/index.html`POST`[18/Aug/2014:14:36:51 +0800]`{\"data\":[{\"httpheader_received_time2\":0,\"module\":\"song\",\"song_id\":3657916,\"is_buffer\":0,\"dnsdone_time2\":2,\"file_size\":5677347,\"buffer_time\":9386,\"buffer_size\":0,\"buffer_count\":0,\"httpheader_received_time\":0,\"postion\":147,\"origin\":\"library-music-category_DJ舞曲_9d474442-7934-41b6-9a5e-68b006d31559\",\"connetdone_time\":751,\"dnsdone_time\":158,\"type\":\"listen_info\",\"url\":\"http:\\/\\/10.0.0.1\\/bav.kvb.yymommy.com\\/mp3_128_36\\/fa\\/b8\\/fa154671971ef6d3b17e2ccea753a6b8.mp3?k=6f68c8ff89c68367&t=1408775196\",\"v\":1,\"time\":1408343927599,\"response_time\":985,\"play_time\":354,\"loading_time\":0,\"connetdone_time2\":0,\"song_time\":354,\"key\":0,\"play_control\":1},{\"module\":\"song\",\"song_id\":3537336,\"is_buffer\":0,\"file_size\":0,\"buffer_time\":11973248,\"buffer_size\":0,\"buffer_count\":0,\"httpheader_received_time\":0,\"postion\":148,\"origin\":\"library-music-category_DJ舞曲_9d474442-7934-41b6-9a5e-68b006d31559\",\"connetdone_time\":0,\"dnsdone_time\":212,\"type\":\"listen_info\",\"v\":1,\"time\":1408343571271,\"response_time\":0,\"play_time\":0,\"loading_time\":0,\"song_time\":0,\"key\":0,\"play_control\":1}],\"param\":{\"openudid\":\"598222f0179b7259\",\"uid\":\"358281050386519\",\"f\":\"f198\",\"app\":\"ttpod\",\"hid\":\"\",\"rom\":\"htc%2Fhtccn_chs_cu%2Fg3u%3A4.1.2%2FJZO54K%2F251763.2%3Auser%2Frelease-keys\",\"net\":2,\"v\":\"v7.3.1.2014072413\",\"s\":\"s200\",\"active\":0,\"mid\":\"HTC+301e\",\"imsi\":\"460015156507074\",\"splus\":\"4.1.2%2F16\",\"tid\":196857047},\"time\":1408343927605,\"uuid\":\"15ec90e8-8474-4263-b1f9-83e5ffec6f8c\"}\n112.96.30.140`48592`/ttpod_client/index.html`POST`[18/Aug/2014:14:36:51 +0800]`{\"data\":[{\"optvalue2\":0,\"v\":1,\"time\":\"201408181438\",\"module\":\"startup\",\"value\":1,\"origin\":\"startup\",\"optvalue\":1,\"type\":\"startup\"},{\"optvalue2\":0,\"v\":1,\"time\":\"201408181437\",\"module\":\"audio_effect\",\"value\":1,\"origin\":\"audio-effect-best\",\"optvalue\":0,\"type\":\"show\"}],\"param\":{\"uid\":\"869005010210697\",\"f\":\"f384\",\"v\":\"v6.6.1.2014012410\",\"app\":\"ttpod\",\"hid\":\"\",\"s\":\"s200\",\"rom\":\"unknown\",\"active\":0,\"mid\":\"A4G\",\"imsi\":\"460010415221862\",\"splus\":\"4.1.1%2F15\",\"tid\":0,\"net\":1},\"time\":1408343916922,\"uuid\":\"fc9d727c-8e15-4540-8a1e-b231b00a58c5\"}"

  val stormConf = ConfigReader.createStormConfig("./dbusSpout.xml")

  val fieldInfos = FieldInfoFactory.getFieldInfo(stormConf.get("com.ttpod.log.format").asInstanceOf[String])

  val bolt = new DownLoadStatBolt
  def testGetCdnInfo(){
    val log = Record.createRecord(urlTestLog,fieldInfos)
    val cdn = bolt.getCdnInfo(log.getLogList("request_body.data").get(0))
    assert(cdn != null)
    assert(cdn._1.equals("蓝汛"))
    assert(cdn._2.equals("bav.kvb.yymommy.com"))


    val log2 = Record.createRecord(urlTestLog2,fieldInfos)
    val cdn2 = bolt.getCdnInfo(log2.getLogList("request_body.data").get(0))
    assert(cdn2 != null)
    assert(cdn2._1.equals("蓝汛"))
    assert(cdn2._2.equals("bav.kvb.yymommy.com"))
  }

}
