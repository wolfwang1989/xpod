package com.ttpod.pushkafka.file;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class LogProducer implements Runnable{

    final static public  int DEFULT_BATCHSIZE = 1000;
    static Logger log = LoggerFactory.getLogger(LogProducer.class);
    Properties properties = null;
    ProducerConfig config = null;
    Producer<String, String> producer ;
    String topic;
    int batchSize;
    public LogProducer(Properties prop,String topicName,int size){
        this.properties = prop;
        config = new ProducerConfig(prop);
        producer = new Producer<String, String>(config);
        topic = topicName;
        batchSize = size;
        new Thread(this).start();
    }



    @Override
    public void run() {
        BlockingQueue<String> queue = DataFactory.getDataQue();
        List<KeyedMessage<String,String> > messages = new ArrayList<KeyedMessage<String, String>>(batchSize);
        String  message = null;
        while(true){
            messages.clear();
            String time = Long.toString(System.currentTimeMillis());
            while ( messages.size() < batchSize){
                try {
                    message =  queue.poll(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    log.error("",e);
                }
                if(message == null ){
                    if(messages.isEmpty())
                        continue;
                    else
                        break;
                }
                messages.add(new KeyedMessage<String, String>(topic,time, message));
            }
            try{
                if(!messages.isEmpty()){
                    producer.send(messages);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                log.error("",e);
              
            }
        }
    }
}
