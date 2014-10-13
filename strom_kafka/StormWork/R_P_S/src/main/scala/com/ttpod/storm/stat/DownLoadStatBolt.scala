package com.ttpod.storm.stat;

import backtype.storm.task.TopologyContext
import backtype.storm.topology.base.BaseBasicBolt
import backtype.storm.topology.{OutputFieldsDeclarer, BasicOutputCollector}
import com.ttpod.Record.ILog
import java.lang.Integer
import com.ttpod.storm.ipstat.{ThreadBuffer, Reader}
import java.io.InputStream
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.conf.Configuration
import java.util
import org.joda.time.DateTime
import scala.collection.mutable
import org.slf4j.LoggerFactory
import backtype.storm.tuple.Tuple
import scala.actors.Actor
import org.joda.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util.Locale


/*
 * uid：用户id
 * ip：用户ip
 * nettype:网络类型
 * version:客户端版本
 * time：操作时间
 */

case class PubInfo(uid:String , ip:String , nettype:Int , version:String , time:String){
  val Location = DownLoadStatBolt.reader.get(ip)
  val dateHour:DateTime = PubInfo.fmt.parseDateTime(time)

  val date = StringBuilder.newBuilder.append(dateHour.getYear).append("-").append(dateHour.getMonthOfYear()).append("-").append(dateHour.getDayOfMonth())
    .toString()

  val hour = StringBuilder.newBuilder.append(dateHour.getYear).append("-").append(dateHour.getMonthOfYear()).append("-").append(dateHour.getDayOfMonth())
    .append("-").append(dateHour.getHourOfDay())
    .toString()

}


object PubInfo{
  val  fmt:DateTimeFormatter= new DateTimeFormatterBuilder().appendLiteral("[").appendDayOfMonth(2).appendLiteral("/")
    .appendMonthOfYearShortText().appendLiteral("/").appendYear(4, 0).appendLiteral(":").appendHourOfDay(2).appendLiteral(":")
    .appendMinuteOfHour(2).appendLiteral(":").appendSecondOfMinute(2).appendLiteral(" +0800]").toFormatter().withLocale(Locale.ENGLISH);
}

/*
 * module: 0在线试听  1歌曲下载
 * statType：操作结果
 * cdnInfo：（cdn名，域名)
 * quality:歌曲下载，歌曲质量
 */
case class statKey(module:Int, statType :Int,cdnInfo:(String,String),quality : String = "-1"){

}

/*
 * count:操作次数
 * kadunCount：试听卡顿次数,sum(buffer_count)
 * responseTime:试听请求时间，sum(response_time)
 * loadtime:下载缓冲时间，sum(loading_time)
 */
class statValue(var count:Int = 0,var kadunCount:Int = 0,var responseTime:Int = 0,var loadTime:Int = 0){

}

class DownLoadStatBolt extends BaseBasicBolt{

  override def prepare(stormConf: java.util.Map[_, _],context: TopologyContext){
    DownLoadStatBolt.init(stormConf)
  }
  override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
    try{
      myExecute(input,collector)
    }catch {
      case e:Exception => DownLoadStatBolt.Log.error("excute error",e)
        e.printStackTrace()
      case _ => DownLoadStatBolt.Log.error("unknown excute error")
    }
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {

  }

  def myExecute(input:backtype.storm.tuple.Tuple , collector : BasicOutputCollector ){
    val log : ILog = input.getValue(0).asInstanceOf[ILog]
    val operations:java.util.List[ILog] = log.getLogList("request_body.data")
    var pubInfo:PubInfo = null
    val stats = scala.collection.mutable.Map[statKey,statValue]()
    for(index <- 0 until operations.size() )  {
      val operation = operations.get(index)
      def getIntDefZero(name :String,defVal:Int = 0): Int = {
        operation.getObject(name) match {
          case x:Integer =>x.toInt
          case x:java.lang.Long  => x.toInt
          case _ => defVal
        }
      }
      val stat:statKey = operation.getString("module")  match {
        case "song"     if ("listen_info".equals(operation.getString("type")) && "local".equals(operation.getString("origin"))) =>{
          statKey(DownLoadStatBolt.SONG_LISREN,DownLoadStatBolt.STATTYPE_SONG_LISTEN_SUCCESS_LOCAL,getCdnInfo(operation))
        }
        case "song"     if "listen_info".equals(operation.getString("type")) => {
          getIntDefZero("is_buffer") match{
            case 0 if getIntDefZero("buffer_count") == 0  => statKey(DownLoadStatBolt.SONG_LISREN,DownLoadStatBolt.STATTYPE_SONG_LISTEN_SUCCESS,getCdnInfo(operation))
            case 1 => statKey(DownLoadStatBolt.SONG_LISREN,DownLoadStatBolt.STATTYPE_SONG_LISTEN_SUCCESS,getCdnInfo(operation))
            case 0 => statKey(DownLoadStatBolt.SONG_LISREN,DownLoadStatBolt.STATTYPE_SONG_LISTEN_CUT,getCdnInfo(operation))
            case _ => DownLoadStatBolt.Log.error("buffer_count not in(0,1,null)"); null;
          }
        }
        case "download" if "song".equals(operation.getString("type"))  =>{
          operation.getString("downstatus") match {
            case "success" => statKey(DownLoadStatBolt.SONG_DOWNLOAD,DownLoadStatBolt.STATTYPE_SONG_DOWNLOAD_SUCCESS,getCdnInfo(operation),operation.getString("quality","-1"))
            case "failed"  => statKey(DownLoadStatBolt.SONG_DOWNLOAD,DownLoadStatBolt.STATTYPE_SONG_DOWNLOAD_FAILED,getCdnInfo(operation),operation.getString("quality","-1"))
            case "deleted" => statKey(DownLoadStatBolt.SONG_DOWNLOAD,DownLoadStatBolt.STATTYPE_SONG_DOWNLOAD_DELETED,getCdnInfo(operation),operation.getString("quality","-1"))
            case _ => DownLoadStatBolt.Log.error("downstatus isn't in (success,failed,deleted)"); null;
          }
        }
        case "error" if ("song".equals(operation.getString("type"))  ) => {
          operation.getString("origin") match {
            case "local" => statKey(DownLoadStatBolt.SONG_LISREN,DownLoadStatBolt.STATTYPE_SONG_LISTEN_FAILED_LOCAL,null)
            case    _    => statKey(DownLoadStatBolt.SONG_LISREN,DownLoadStatBolt.STATTYPE_SONG_LISTEN_FAILED,getCdnInfo(operation.getString("origin")))
          }
        }
        case "error" if ("not_url".equals(operation.getString("type") )  ) => {
          operation.getString("origin") match {
            case "song" => statKey(DownLoadStatBolt.SONG_LISREN,DownLoadStatBolt.STATTYPE_SONG_LISTEN_FAILED_NO_URL,getCdnInfo(operation))
            case "download"   => statKey(DownLoadStatBolt.SONG_DOWNLOAD,DownLoadStatBolt.STATTYPE_SONG_DOWNLOAD_FAILED_NO_URL,getCdnInfo(operation))
            case _ => null
          }
        }
        case _  => null;
      }
      if (stat != null ) {

        val cc:Option[statValue] = stats.get(stat)
        if(!cc.isEmpty){
          cc.get.count += 1
          if(operation.getString("module").equals("song")){
            cc.get.kadunCount += getIntDefZero("buffer_count")
            cc.get.loadTime += getIntDefZero("response_time")
            cc.get.loadTime += getIntDefZero("loading_time")
          }
        }
        else{
          if(operation.getString("module").equals("song")){
            stats.put(stat,new statValue(count = 1,kadunCount = getIntDefZero("buffer_count")
                                ,responseTime = getIntDefZero("response_time")
                                ,loadTime = getIntDefZero("loading_time")))
          }else{
            stats.put(stat,new statValue(1));
          }

        }

      }
    }
    if(!stats.isEmpty) {
        DownLoadStatBolt.writeToHbase ! (stats,PubInfo(log.getString("request_body.param.uid"),
          log.getString("remote_addr"),
          log.getInt("request_body.param.net"),
          log.getString("request_body.param.v"),
          log.getString("time_local")))
    }
  }
  def getCdnInfo(url:String):(String,String) = {
    if(url == null){
      return  null
    }
    val index = url.indexOf("/",7)
    if(index == -1)
      return null
    val domain = {
      url.substring(7,index) match{
        case e:String if e.endsWith("com") => e
        case _ => url.substring(index+1,url.indexOf("/",index + 1))
      }
    }
    val name = DownLoadStatBolt.cndToDomain.get(domain)
    if(name.isEmpty)
      return null
    (name.get,domain)
  }

  def getCdnInfo(log:ILog):(String,String) = {
        val url = log.getString("url");
        getCdnInfo(url)
  }
}




object DownLoadStatBolt{

  val Log = LoggerFactory.getLogger("DownLoadStatBolt")
  val cndToDomain = Map[String,String] ("bav.kvb.yymommy.com" -> "蓝汛","hzd.exi.hotchaleur.com" -> "蓝汛",
    "jdlbqc.tgg.yymommy.com" -> "网宿", "ws.cs.hotchanson.com" -> "网宿",
    "nie.dfe.yymommy.com" -> "云端","nmo.ouj.yymommy.com" -> "云端","oen.cye.yymommy.com" -> "云端",
    "a.ali.dongting.com" -> "阿里", "b.ali.hotchanson.com" -> "阿里"
  )
  val noDaLu = Set("香港省","台湾省","澳门省")

  val SONG_LISREN = 0
  val SONG_DOWNLOAD = 1
  val STATTYPE_SONG_LISTEN_SUCCESS = 0
  val STATTYPE_SONG_LISTEN_CUT = 1
  val STATTYPE_SONG_LISTEN_FAILED = 2
  val STATTYPE_SONG_LISTEN_FAILED_NO_URL = 3

  val STATTYPE_SONG_LISTEN_SUCCESS_LOCAL = 10
  val STATTYPE_SONG_LISTEN_FAILED_LOCAL = 11
  val STATTYPE_SONG_LISTEN_CUT_LOCAL = 12

  val STATTYPE_SONG_DOWNLOAD_SUCCESS = 0
  val STATTYPE_SONG_DOWNLOAD_FAILED = 1
  val STATTYPE_SONG_DOWNLOAD_DELETED = 2
  val STATTYPE_SONG_DOWNLOAD_FAILED_NO_URL = 3

  val reader:Reader = {
    val inputStream: InputStream = ClassLoader.getSystemResourceAsStream("my_ip.ttdb")
    val buffer = new ThreadBuffer(inputStream)
    inputStream.close()
    new Reader(buffer,"my_ip.ttdb");

  }

  import scala.actors.Actor._
  import org.apache.hadoop.hbase.client.Row
  class timer(val actor :Actor,val period :Int = 60000) extends Actor{
    override def act(): Unit = {
      Log.info("enter timer")
      while(true){
        Thread.sleep(period)
        actor ! "tick"
        Log.info("timer send one")
      }
      Log.info("exit timer")
    }
  }

  class Write(val stormConf: java.util.Map[_, _]) extends Actor{
    override def act(): Unit = {
      val conf = new  Configuration()
      conf.set("hbase.zookeeper.quorum", stormConf.get("hbase.zookeeper.quorum").asInstanceOf[String])

      conf.set("hbase.zookeeper.property.clientPort", {
        if(stormConf.get("hbase.zookeeper.property.clientPort") != null)
          stormConf.get("hbase.zookeeper.property.clientPort").asInstanceOf[String]
        else
          "2181"
      })
      Log.info("1")
//      val hTableFactory = new HTableFactory();
      val hconnection = HConnectionManager.createConnection(conf)
      Log.info("2")
      loop{
        receive{
          case (actions:Any , tableName:String) =>{
            Log.info("3")
            val table = hconnection.getTable(tableName.getBytes())
            val listAcc  = new util.ArrayList[Row]()
            Log.info("write size =  " + actions.asInstanceOf[mutable.HashMap[String,mutable.HashMap[String,Int]]].size)
            for((key:String , cntInfo:mutable.HashMap[String,Int]) <- actions.asInstanceOf[mutable.HashMap[String,mutable.HashMap[String,Int]]]) {
              val inc = new Increment(key.getBytes)
              for( (field:String,count:Int) <- cntInfo if count > 0)
                inc.addColumn("stat".getBytes, field.getBytes() , count)
              listAcc.add(inc)
            }

            table.batch(listAcc)
            table.close()
          }
          case _ =>
        }
      }
    }
  }
  class WriteToHbase(val stormConf: java.util.Map[_, _],val writer:Actor) extends Actor{
    val clock = new timer(this,
      {
        if(stormConf.get("com.ttpod.writetohbase.internal") != null)
          stormConf.get("com.ttpod.writetohbase.internal").asInstanceOf[java.lang.Long].toInt
        else
          60000
      }
    )
    def  add(container : mutable.HashMap[ String,mutable.HashMap[String,Int] ],firstKey:String,secondKey:String,count:Int){
      val firstVal = container.get(firstKey)
      if(!firstVal.isEmpty){
        val secondVal = firstVal.get.get(secondKey)
        if(!secondVal.isEmpty){
          firstVal.get.update(secondKey,secondVal.get + count);
        }else{
          firstVal.get.put(secondKey,count);
        }
      }
      else{
        container.put(firstKey, mutable.HashMap(secondKey -> count));
      }
    }

    def  mergeSong(container : mutable.HashMap[ String,mutable.HashMap[String,Int]],stat:statKey,count:statValue,pubinfo : PubInfo ){
      val keys = new util.ArrayList[String]()
      val locs = new util.ArrayList[String]()
      if(pubinfo.Location.country != null && !pubinfo.Location.country.isEmpty && pubinfo.Location.country == "中国" && !noDaLu.contains(pubinfo.Location.province)){
        locs.add(pubinfo.Location.country)
        if(pubinfo.Location.province != null && !pubinfo.Location.province.isEmpty ){
          locs.add(pubinfo.Location.country + "_" + pubinfo.Location.province)
          if(pubinfo.Location.city != null && !pubinfo.Location.city.isEmpty)
            locs.add(pubinfo.Location.country + "_" + pubinfo.Location.province + "_" + pubinfo.Location.city)
        }
      }else return
      if(pubinfo.Location.isp != null && !pubinfo.Location.isp.isEmpty)
        keys.add(pubinfo.Location.isp);
      if(stat.cdnInfo != null){
        keys.add(stat.cdnInfo._1);
        keys.add(stat.cdnInfo._1 + "_" + stat.cdnInfo._2)
      }
      keys.add(pubinfo.version)
      keys.add(pubinfo.nettype.toString)
      for(locI <- 0 until locs.size() ){
        val loc = locs.get(locI)
        add(container,loc +  "_" + pubinfo.date,"totalcount",count.count)
        add(container,loc +  "_" + pubinfo.date,"totalkaduncount",count.kadunCount)
        add(container,loc +  "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
        add(container,loc +  "_" + pubinfo.date,"totalrespondtime",count.responseTime)
        add(container,loc +  "_" + pubinfo.date,"totalloadtime",count.loadTime)

        add(container,loc +  "_" + pubinfo.hour,"totalcount",count.count)
        add(container,loc +  "_" + pubinfo.hour,"totalkaduncount",count.kadunCount)
        add(container,loc +  "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
        add(container,loc +  "_" + pubinfo.hour,"totalrespondtime",count.responseTime)
        add(container,loc +  "_" + pubinfo.hour,"totalloadtime",count.loadTime)
      }
      for(i <- 0 until keys.size() if(!keys.get(i).isEmpty)){
        for(j <- 0 until keys.size() if(!keys.get(j).isEmpty)){
          if(i == j){
            add(container,keys.get(i) +  "_" + pubinfo.date,"totalcount",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.date,"totalkaduncount",count.kadunCount)
            add(container,keys.get(i) +  "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.date,"totalrespondtime",count.responseTime)
            add(container,keys.get(i) +  "_" + pubinfo.date,"totalloadtime",count.loadTime)

            add(container,keys.get(i) +  "_" + pubinfo.hour,"totalcount",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.hour,"totalkaduncount",count.kadunCount)
            add(container,keys.get(i) +  "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.hour,"totalrespondtime",count.responseTime)
            add(container,keys.get(i) +  "_" + pubinfo.hour,"totalloadtime",count.loadTime)

            for(locIndex <- 0 until locs.size() - 1){
              val loc = locs.get(locIndex)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"totalcount",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"totalkaduncount",count.kadunCount)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"totalrespondtime",count.responseTime)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"totalloadtime",count.loadTime)

              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"totalcount",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"totalkaduncount",count.kadunCount)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"totalrespondtime",count.responseTime)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"totalloadtime",count.loadTime)
            }

          }
          else{
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"totalcount",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"totalkaduncount",count.kadunCount)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"totalrespondtime",count.responseTime)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"totalloadtime",count.loadTime)

            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"totalcount",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"totalkaduncount",count.kadunCount)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"totalrespondtime",count.responseTime)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"totalloadtime",count.loadTime)
          }

        }
      }
    }

    def  mergeDownload(container : mutable.HashMap[ String,mutable.HashMap[String,Int]],stat:statKey,count:statValue,pubinfo : PubInfo ){
      val keys = new util.ArrayList[String]()
      val locs = new util.ArrayList[String]()
      if(pubinfo.Location.country != null && !pubinfo.Location.country.isEmpty && pubinfo.Location.country == "中国" && !noDaLu.contains(pubinfo.Location.province)){
        locs.add(pubinfo.Location.country)
        if(pubinfo.Location.province != null && !pubinfo.Location.province.isEmpty ){
          locs.add(pubinfo.Location.country + "_" + pubinfo.Location.province)
          if(pubinfo.Location.city != null && !pubinfo.Location.city.isEmpty)
            locs.add(pubinfo.Location.country + "_" + pubinfo.Location.province + "_" + pubinfo.Location.city)
        }
      }else return
      if(pubinfo.Location.isp != null && !pubinfo.Location.isp.isEmpty)
        keys.add(pubinfo.Location.isp);
      if(stat.cdnInfo != null){
        keys.add(stat.cdnInfo._1);
        keys.add(stat.cdnInfo._1 + "_" + stat.cdnInfo._2)
      }

      keys.add(pubinfo.version)
      keys.add(pubinfo.nettype.toString)
      for(locI <- 0 until locs.size() ){
        val loc = locs.get(locI)
        add(container,loc +  "_" + pubinfo.date,"totalcount",count.count)
        add(container,loc +  "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
        add(container,loc +  "_" + pubinfo.date,"quality_" + stat.quality + "_count",count.count)

        add(container,loc +  "_" + pubinfo.hour,"totalcount",count.count)
        add(container,loc +  "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
        add(container,loc +  "_" + pubinfo.hour,"quality_" + stat.quality + "_count",count.count)
      }
      for(i <- 0 until keys.size() if(!keys.get(i).isEmpty)){
        for(j <- 0 until keys.size() if(!keys.get(j).isEmpty)){
          if(i == j){
            add(container,keys.get(i) +  "_" + pubinfo.date,"totalcount",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.date,"quality_" + stat.quality + "_count",count.count)

            add(container,keys.get(i) +  "_" + pubinfo.hour,"totalcount",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_" + pubinfo.hour,"quality_" + stat.quality + "_count",count.count)

            for(locIndex <- 0 until locs.size() - 1){
              val loc = locs.get(locIndex)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"totalcount",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.date,"quality_" + stat.quality + "_count",count.count)

              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"totalcount",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
              add(container,keys.get(i) +  "_" + loc + "_" + pubinfo.hour,"quality_" + stat.quality + "_count",count.count)
            }

          }
          else{
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"totalcount",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.date,"quality_" + stat.quality + "_count",count.count)

            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"totalcount",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"stattype_" + stat.statType + "_count",count.count)
            add(container,keys.get(i) +  "_"  + keys.get(j) + "_" + pubinfo.hour,"quality_" + stat.quality + "_count",count.count)
          }

        }
      }
    }

    override def act(): Unit = {
      clock.start()

      var songKeyCount = mutable.HashMap[ String,mutable.HashMap[String,Int] ] ()
      var downKeyCount = mutable.HashMap[ String,mutable.HashMap[String,Int] ] ()

      loop{
        receive{
          case (stats:Any , pubinfo : PubInfo) => {
//            Log.("get stats")
            for( (stat:statKey,count:statValue) <- stats.asInstanceOf[mutable.HashMap[statKey,statValue]]){
              stat match {
                case statKey(DownLoadStatBolt.SONG_LISREN,_,_,_) => {
                  mergeSong(songKeyCount,stat,count,pubinfo)
                }
                case statKey(DownLoadStatBolt.SONG_DOWNLOAD,_,_,_) =>{
                  mergeDownload(downKeyCount,stat,count,pubinfo)
                }
              }
            }
          }
          case str:String  => {
            Log.info("tick: song_stat size = " + songKeyCount.size + " ; download_stat size = " + downKeyCount.size)
            if(songKeyCount.size > 0)
              writer ! (songKeyCount,"song_stat")
            if(downKeyCount.size > 0)
              writer ! (downKeyCount,"download_stat")
            songKeyCount = mutable.HashMap[ String,mutable.HashMap[String,Int] ] ()
            downKeyCount = mutable.HashMap[ String,mutable.HashMap[String,Int] ] ()
          }
          case _ => Log.error("recieved error message")
        }
      }
    }
  }

  var writeToHbase:Actor = null

  def init(stormConf: java.util.Map[_, _]){

    if(writeToHbase == null){
      synchronized{
        val write = new Write(stormConf)
        if(writeToHbase == null){
          writeToHbase = new WriteToHbase(stormConf,write)
          write.start()
          writeToHbase.start()
        }
      }
    }
  }

}