java -Xmx1000m -server -XX:+UseConcMarkSweepGC -XX:-CMSConcurrentMTEnabled -XX:+UseParNewGC  -Dlog4j.configuration=file:./log4j.properties  -cp ./PushToKafka-1.0-SNAPSHOT-jar-with-dependencies.jar    com.ttpod.pushkafka.file.Main pull.config &