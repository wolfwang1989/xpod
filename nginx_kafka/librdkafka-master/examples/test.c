
#include <ctype.h>
#include <signal.h>
#include <string.h>
#include <errno.h>
#include "rdkafka.h"  /* for Kafka driver */
/* Do not include these defines from your program, they will not be
 *  * provided by librdkafka. */
#include "rd.h"
#include "rdtime.h"


static rd_kafka_conf_t *conf;
static rd_kafka_topic_conf_t *topic_conf;
static rd_kafka_t* produce;
static rd_kafka_conf_t * createConf(){
	rd_kafka_conf_t* tmpConf = rd_kafka_conf_new();
	rd_kafka_conf_set(tmpConf, "queue.buffering.max.messages", "10000",
			  NULL, 0);
	rd_kafka_conf_set(tmpConf, "message.send.max.retries", "3", NULL, 0);
	rd_kafka_conf_set(tmpConf, "retry.backoff.ms", "500", NULL, 0);
	rd_kafka_conf_set(tmpConf, "compression.codec","snappy",NULL, 0);
	return tmpConf;
}
static rd_kafka_topic_conf_t * createTopicConf(){
	rd_kafka_topic_conf_t* tmpTopicConf = rd_kafka_topic_conf_new();
	return tmpTopicConf;
}

static rd_kafka_t* createProduce(char* brokerList){
	
	rd_kafka_t* tmpPro = rd_kafka_new(RD_KAFKA_PRODUCER, conf,NULL, 0);
	rd_kafka_brokers_add(tmpPro, brokerList);
	return tmpPro;
}
static rd_kafka_topic_t * createProduceTopic(const char *topicName){
	return rd_kafka_topic_new(produce,topicName,topic_conf);
}
static int verbosity =2;
static void rd_kafka_produce_random_copy_no_key (rd_kafka_topic_t *topic,
		      void *payload, size_t len) {
	int cnt = 0;
	while( cnt < 3 && rd_kafka_produce(topic, RD_KAFKA_PARTITION_UA,
				RD_KAFKA_MSG_F_COPY, payload, len,
				NULL, 0, NULL) == -1){
				if (errno == ESRCH)
					printf("%% No such partition: "
                                               "%"PRId32"\n", RD_KAFKA_PARTITION_UA);
				else if ((errno != ENOBUFS && verbosity >= 1) ||
                                         verbosity >= 3)
					printf("%% produce error: %s%s\n",
					       rd_kafka_err2str(
						       rd_kafka_errno2err(
							       errno)),
					       errno == ENOBUFS ?
					       " (backpressure)":"");
				if (errno != ENOBUFS) {
					printf("%% isn't ENOBUFS error: \n");
					break;
				}
				++cnt;
				rd_kafka_poll(produce, 10);
	}
	return ;
}

static void init(char* brokerList){
	conf = createConf();
	if(!conf){
		printf("init conf failed!!!\n");
		exit(0);
	}
	topic_conf = createTopicConf();
	if(!topic_conf){
		printf("init topic_conf failed!!!\n");
                exit(0);
	}
	produce = createProduce(brokerList);
	if(!produce){
		printf("init produce failed!!!\n");
                exit(0);
	}
}
int main(){
	init("hadoop01:9092");
	rd_kafka_topic_t* topic = createProduceTopic("test");
	char* a = (char*) malloc(5);
	memcpy(a,"hello",5);
	int n = 100000;
	while(n-- > 0 )
		rd_kafka_produce_random_copy_no_key(topic,a,5);
	free(a);
	sleep(2);
	return 0;
}
