package com.ttpod.storm.ipstat;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import com.ttpod.Record.FieldInfo;
import com.ttpod.Record.FieldInfoFactory;
import com.ttpod.Record.Record;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Administrator on 14-6-18.
 */
public class PullFromKafkaSpout extends BaseRichSpout {

    static Logger log = LoggerFactory.getLogger(PullFromKafkaSpout.class);

    static private List<FieldInfo> fieldInfos = null;

    private ConsumerConnector _consumer = null;
    private String topic;
    private SpoutOutputCollector _collector;
    private ConsumerIterator<byte[], byte[]> _it;


    private static ConsumerConfig createConsumerConfig(Map conf) {
        Properties props = new Properties();
        props.put("zookeeper.connect", conf.get("kafka.zookeeper.connect"));
        props.put("group.id", conf.get("kafka.group.id"));
        props.put("socket.receive.buffer.bytes", conf.get("kafka.socket.receive.buffer.bytes"));
        props.put("fetch.message.max.bytes", conf.get("kafka.fetch.message.max.bytes"));
        props.put("zookeeper.connection.timeout.ms", "60000");
        return new ConsumerConfig(props);

    }


    @Override
    public void open(Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {
        // TODO Auto-generated method stub
        this.topic = (String) conf.get("dbus.spout.kafka.topicname");
        _collector = collector;
        _consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(conf));
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = _consumer.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0);
        _it = stream.iterator();
        synchronized (log) {
            if (fieldInfos == null) {
                fieldInfos = FieldInfoFactory.getFieldInfo((String) conf.get("com.ttpod.log.format"));
            }
            if (fieldInfos == null || fieldInfos.size() == 0) {
                log.error("parse com.ttpod.log.format failed");
                System.exit(0);
            }
        }
    }

    @Override
    public void nextTuple() {
        if (_it.hasNext()) {
            String e = new String(_it.next().message());
            List<Object> obj = new ArrayList<Object>();
            try {
                Record record = Record.createRecord(e, fieldInfos);
                if (record != null)
                    obj.add(record);

            } catch (Exception e1) {
                log.error("create record failed:" + e, e1);
                return;
            }
            if (!obj.isEmpty())
                _collector.emit(obj);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("log"));
    }

    ;

}
