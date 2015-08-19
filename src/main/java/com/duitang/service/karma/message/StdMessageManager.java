package com.duitang.service.karma.message;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * 标准主谓宾事件消息发送器
 * 
 * @author kevx
 * @since 1:39:09 PM Aug 19, 2015
 */
public class StdMessageManager {

    private final Logger log = Logger.getLogger("main");
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    private Producer<String, String> producer;
    
    private String batchNumMessages = "200";
    private String producerType = "sync";
    private String requestRequiredAcks = "0";
    
    protected Properties prepareKafkaParams() {
        Properties props = new Properties();
        props.put("metadata.broker.list", "std-1.kafka.infra.duitang.net:9092,std-2.kafka.infra.duitang.net:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "0");
        props.put("producer.type", producerType);
        props.put("queue.enqueue.timeout.ms", "-1");
        props.put("batch.num.messages", batchNumMessages);
        props.put("compression.codec", "1");
        return props;
    }
    
    public void init() {
        producer = new Producer<String, String>(new ProducerConfig(prepareKafkaParams()));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (producer != null) producer.close();
            }
        });
    }
    
    public void send (String topic, StdMessage msg) {
        try {
            String msgstr = mapper.writeValueAsString(msg);
            producer.send(new KeyedMessage<String, String>(
                    topic, String.valueOf(Math.random()), msgstr
            ));
        } catch (JsonProcessingException e) {
            log.error("StdMessageManager_failed", e);
        }
    }

    public String getBatchNumMessages() {
        return batchNumMessages;
    }

    public void setBatchNumMessages(String batchNumMessages) {
        this.batchNumMessages = batchNumMessages;
    }

    public String getProducerType() {
        return producerType;
    }

    public void setProducerType(String producerType) {
        this.producerType = producerType;
    }

    public String getRequestRequiredAcks() {
        return requestRequiredAcks;
    }

    public void setRequestRequiredAcks(String requestRequiredAcks) {
        this.requestRequiredAcks = requestRequiredAcks;
    }
    
}
