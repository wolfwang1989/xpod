package com.ttpod.storm.recomend

import backtype.storm.topology.{OutputFieldsDeclarer, BasicOutputCollector, IBasicBolt}
import backtype.storm.topology.base.BaseBasicBolt
import backtype.storm.tuple.Tuple
import com.ttpod.Record.ILog
import scala.collection.mutable.ListBuffer
import com.ttpod.storm.stat.DownLoadStatBolt
import com.ttpod.storm.ipstat.Location
import scala.actors.Actor._
import org.apache.hadoop.hbase.client.{HConnectionManager, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.conf.Configuration
import java.util
import scala.actors.Actor
import backtype.storm.task.TopologyContext
import org.joda.time.format.{DateTimeFormatterBuilder, DateTimeFormatter}
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import java.lang.management.{RuntimeMXBean, ManagementFactory}
import org.slf4j.LoggerFactory
import org.apache.hadoop.hbase.{CellUtil, Cell}


/**
 * Created by Administrator on 14-8-14.
 */

/**
 *  store the public info for all operations in one record
 *@param ids   tuple, _1 uid, _2 tuid
 *@param channel
 *@param version  the version of app
 *@param net   net type, -1 none, 0 2G, 1 wap, 2 wifi, 3 3G
 *@param app   application name, ttpod present music player, fm present  FM
 *@param location  ip address,includes country,province,city and isp
 *@param devType  device type, 0 phone,1 pad, 2 HD ,3 voice box
 */
case class PubInfo(ids:(String,String),channel:String,version:String,net:Int,app:String,location:Location,devType :Int){
  def addInfoToPut(put:Put){
    if(channel != null)
      put.add("stat".getBytes,"s".getBytes,channel.getBytes)
    if(version != null)
      put.add("stat".getBytes,"v".getBytes,version.getBytes)
    put.add("stat".getBytes,"net".getBytes,Bytes.toBytes(net))
    if(app != null)
      put.add("stat".getBytes,"app".getBytes,Bytes.toBytes(app))
    if(location.isp != null && !location.isp.isEmpty)
      put.add("stat".getBytes,"isp".getBytes,Bytes.toBytes(location.isp))
    if(location.country != null && !location.country.isEmpty)
      put.add("stat".getBytes,"country".getBytes,Bytes.toBytes(location.country))
    if(location.city != null && !location.city.isEmpty)
      put.add("stat".getBytes,"city".getBytes,Bytes.toBytes(location.city))
    if(location.province != null && !location.province.isEmpty)
      put.add("stat".getBytes,"province".getBytes,Bytes.toBytes(location.province))
    if(ids._1 != null && !ids._1.isEmpty)
      put.add("stat".getBytes,"devId".getBytes,Bytes.toBytes(ids._1))
    if(ids._2 != null && !ids._2.isEmpty)
      put.add("stat".getBytes,"userid".getBytes,Bytes.toBytes(ids._2))
    put.add("stat".getBytes,"devtype".getBytes,Bytes.toBytes(devType))
  }

}


abstract class PutTrait{
  val time:Long
  val seq : String
  def addInfo(put:Put);
  def createPut(pubInfo:PubInfo,buffer:ListBuffer[Put]):Unit = {
    val t = (RecomendPro.timeReducer - time)
    if(pubInfo.ids._1 != null && !pubInfo.ids._1.isEmpty){
      val put = new Put(((pubInfo.ids._1) + "_" + "u" + "_" + t + "_" + seq).getBytes)
      addInfo(put)
      pubInfo.addInfoToPut(put)
      buffer += put
    }
    if(pubInfo.ids._2 != null && !pubInfo.ids._2.isEmpty){
      val put = new Put(((pubInfo.ids._2) + "_" + "t" + "_" + t + "_" + seq).getBytes)
      addInfo(put)
      pubInfo.addInfoToPut(put)
      buffer += put
    }
  }
}

/**
 *
 * @param songId
 * @param playTime  how long play the song
 * @param time    when play the song
 */
case class ListenInfo(songId:String,playTime:Int,override val time:Long,override val seq:String) extends PutTrait {
  override def addInfo(put:Put){
    if(songId != null)
      put.add("stat".getBytes,"song_id".getBytes,Bytes.toBytes(songId))
    put.add("stat".getBytes,"tlen".getBytes,Bytes.toBytes(playTime))
    put.add("stat".getBytes,"time".getBytes,Bytes.toBytes(time))
    put.add("stat".getBytes,"module".getBytes,Bytes.toBytes("listen"))
  }


}

/**
 *
 * @param songId
 * @param downLoadTime
 * @param time
 */
case class DownLoadInfo(songId:String,downLoadTime:Long,override val time:Long,override val seq:String) extends PutTrait{
  override def addInfo(put:Put){
    put.add("stat".getBytes,"song_id".getBytes,Bytes.toBytes(songId))
    put.add("stat".getBytes,"tlen".getBytes,Bytes.toBytes(downLoadTime))
    put.add("stat".getBytes,"time".getBytes,Bytes.toBytes(time))
    put.add("stat".getBytes,"module".getBytes,Bytes.toBytes("download"))
  }


}


case class RankListInfo(rankId:String,override val time:Long,override val seq:String) extends PutTrait{
  override def addInfo(put:Put){
    put.add("stat".getBytes,"rank_id".getBytes,Bytes.toBytes(rankId))
    put.add("stat".getBytes,"time".getBytes,Bytes.toBytes(time))
    put.add("stat".getBytes,"module".getBytes,Bytes.toBytes("song_list"))
  }

}

/**
 * do process
 */
class RecomendPro extends BaseBasicBolt{



  var seq = 100000
  var seed:Int  = 0

  private def getSeq() = {
    if(seq >= 999999){
      seq = 100000
    }else{
      seq += 1
    }
    StringBuilder.newBuilder.append(seed).append(seq).append("_").append(RecomendPro.processId).toString()
  }

  def getPubInfo(log:ILog):PubInfo = {
    val ip = log.getString("remote_addr")
    if(ip != null){
      val ipAddr:Location = DownLoadStatBolt.reader.get(ip)
      val uid = {
        log.getString("request_body.param.uid") match{
          case e:String if !e.isEmpty => e
          case _ =>  log.getString("request_body.param.uuid")
        }
      }

      val tuid = {
        log.getObject("request_body.param.tid") match{
          case e:String if !e.isEmpty => e
          case e:java.lang.Long if e > 0 => e.toString
          case e:java.lang.Integer if e > 0 => e.toString
          case _ => null
        }
      }

      if(uid == null && tuid == null){
        return null
      }

      val devType:Int = log.getString("request_uri") match{
        case x if x.matches("(.*/ttpod_client/.*|.*/ttpod_ios_client/.*)") => 0
        case x if x.matches(".*/ttpod_ios_pad/.*") => 1
        case x if x.matches(".*/ttpod_client_hd/.*") => 2
        case _ => 0
      }
      PubInfo((uid,tuid),log.getString("request_body.param.s")
        ,log.getString("request_body.param.v")
        ,RecomendPro.safeGetNumberInt(log,"request_body.param.net",-1)
        ,log.getString("request_body.param.app")
        ,ipAddr,devType);
    }
    else
      null
  }

  override def prepare(stormConf:java.util.Map[_, _], context:TopologyContext ) {
    RecomendPro.init(stormConf)
    seed = RecomendPro.sequence.addAndGet(1)
    println("seed value is " + seed)
  }

  override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
    try{
      myExecute(input,collector)
    }
    catch {
      case e:Exception => RecomendPro.logger.error("",e)
    }
  }

  def myExecute(input: Tuple, collector: BasicOutputCollector): Unit = {
    val log = input.getValue(0).asInstanceOf[ILog]
    val logList = log.getLogList("request_body.data")
    val pubInfo :PubInfo = getPubInfo(log)
    if(pubInfo == null)
      return
    val  ops = ListBuffer[Put]()
    var time = RecomendPro.fmt.parseMillis(log.getString("time_local"))
    for(index <- 0 until logList.size() ){
      time += 1
      val operation = logList.get(index)
      val data:PutTrait =  operation.getString("module") match {
        case "song" if "listen_info".equals(operation.getString("type")) =>
          val tlen = RecomendPro.safeGetNumberLong(operation,"play_time",0)
          val songId = operation.getObject("song_id") match{
            case null => null
            case e => e.toString
          }
          if(songId != null && !songId.isEmpty)
            ListenInfo(songId,tlen.toInt * 1000, time,getSeq)
          else
            null

        case "download" if "song".equals(operation.getString("type")) =>
          val songId = operation.getObject("fileid") match{
            case null => null
            case e => e.toString
          }
          val tlen = RecomendPro.safeGetNumberLong(operation,"download_time",0)
          if(songId != null && !songId.isEmpty)
            DownLoadInfo(songId,tlen.toInt,time,getSeq)
          else
            null

        case "listen_info" if "listen".equals(operation.getString("type")) =>
          val songId = operation.getObject("songid") match{
            case null => null
            case e => e.toString
          }
          val tlen = RecomendPro.safeGetNumberLong(operation,"play_time",0)
          if(songId != null && !songId.isEmpty)
            ListenInfo(songId,tlen.toInt * 1000,time,getSeq)
          else
            null
        case "find_music" =>
          operation.getString("type") match{
            case  "listen" => {
              val rankId = operation.getString("optmessage",null)
              if(rankId != null && !rankId.isEmpty)
                RankListInfo(rankId, time,getSeq)
              else
                null
            }
            case  "rank_show" |"library_show"  if  operation.getString("origin","").contains("play") => {
              val rankId = operation.getString("optmessage",null)
              if(rankId != null && !rankId.isEmpty)
                RankListInfo(rankId,time,getSeq)
              else
                null
            }
            case _ => null
          }

        case _ => null
      }
      if(data != null)
        data.createPut(pubInfo,ops)
    }
    if (ops.size != 0 && pubInfo != null ){
      RecomendPro.receiver ! ops.toList
    }
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {

  }
}


object RecomendPro{
  val logger = LoggerFactory.getLogger("RecomendPro")

  val timeReducer = 9999999999999L


  /**
   * get current process id
   */
  private def  getCurrentThreadID() :Integer =
  {
    val runtime = ManagementFactory.getRuntimeMXBean();
    val name = runtime.getName();
    Integer.parseInt(name.substring(0, name.indexOf("@")));
  }

  val processId = getCurrentThreadID.toString
  val sequence = new AtomicInteger(1000);


  /**
   *  the log time format
   */
  val  fmt:DateTimeFormatter= new DateTimeFormatterBuilder().appendLiteral("[").appendDayOfMonth(2).appendLiteral("/")
    .appendMonthOfYearShortText().appendLiteral("/").appendYear(4, 0).appendLiteral(":").appendHourOfDay(2).appendLiteral(":")
    .appendMinuteOfHour(2).appendLiteral(":").appendSecondOfMinute(2).appendLiteral(" +0800]").toFormatter().withLocale(Locale.ENGLISH);

  val lock = 1;
  def init(stormConf:java.util.Map[_, _]){
    synchronized{
      if(writer == null) {
        writer = new Writer(stormConf)
      }else{
        return
      }
      if(receiver == null){
        receiver = new Receiver();
      }else{
        return
      }

      val preid:Int = {
        stormConf.get("com.ttpod.writetohbase.internal") match {
          case e:java.lang.Long => e.toInt
          case e:java.lang.Integer => e.toInt
          case _ => 60000
        }

      }
      receiver.start()
      writer.start()
      val timer = actor{
        while(true){
          while(true){
            Thread.sleep(preid)
            receiver ! "send"
          }
        }
      }
    }
  }


  class Writer(stormConf:java.util.Map[_, _]) extends Actor{
    val conf = new  Configuration()
    override def act(): Unit = {
      conf.set("hbase.zookeeper.quorum", stormConf.get("hbase.zookeeper.quorum").asInstanceOf[String])
      conf.set("hbase.zookeeper.property.clientPort", {
        if(stormConf.get("hbase.zookeeper.property.clientPort") != null)
          stormConf.get("hbase.zookeeper.property.clientPort").asInstanceOf[String]
        else
          "2181"
      })

      val hconnection = HConnectionManager.createConnection(conf)
      val tableName = stormConf.get("com.ttpod.recmend.user.action.tablename").asInstanceOf[String]

      while(true){
        receive{
          case puts :util.ArrayList[Put] =>{
            if(logger.isDebugEnabled){
              for(index <- 0 until puts.size()){
                logger.info("rowkey:" + Bytes.toString(puts.get(index).getRow))
                val s = puts.get(index).cellScanner()

                while(s.advance())
                {
                  val c = s.current()
                  val key = Bytes.toString(CellUtil.cloneQualifier(c))
                  key match {
                    case "time" => logger.info(key + ":" + Bytes.toLong(CellUtil.cloneValue(c)));
                    case "tlen" | "net" | "devtype" => logger.info(key + ":" + Bytes.toInt(CellUtil.cloneValue(c)));
                    case _ => logger.info(key + ":" + Bytes.toString(CellUtil.cloneValue(c)));
                  }
                }
              }
            }

            logger.info("write size = " + puts.size())
            val table = hconnection.getTable(tableName.getBytes())
            table.put(puts)
            table.close()
          }
          case _ =>
        }
      }
    }
  }

  class Receiver extends Actor{
    override def act(): Unit = {
      var store = new util.ArrayList[Put](10000)
      while (true) {
        receive {
          case e:List[Put] =>
            for(i <- 0 until e.size){
              store.add(e(i))
            }
          case "send" =>{
            writer ! store
            logger.info("send size = " + store.size())

            store = new util.ArrayList[Put](10000)
          }
          case _ =>
        }
      }
    }
  }

  var writer:Writer = null;

  /**
   * recieve and store the data, send to writer
   */
  var  receiver:Receiver = null

  def safeGetNumberLong(operation:ILog,key:String,defval:Long = 0):Long = {
    operation.getObject(key) match {
      case e:java.lang.Long => e
      case e:java.lang.Integer => e.toLong
      case e:java.lang.String => {
        try{
          java.lang.Long.parseLong(e)
        }catch {
          case _ => defval
        }
      }
      case _ => defval
    }
  }

  def safeGetNumberInt(operation:ILog,key:String,defval:Int = 0): Int = {
    operation.getObject(key) match {
      case e:java.lang.Long => e.toInt
      case e:java.lang.Integer => e
      case e:java.lang.String => {
        try{
          java.lang.Integer.parseInt(e)
        }catch {
          case _ => defval
        }
      }
      case _ => defval
    }
  }

}