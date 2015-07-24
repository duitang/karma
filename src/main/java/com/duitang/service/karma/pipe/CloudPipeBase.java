package com.duitang.service.karma.pipe;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author kevx
 * @since 10:49:58 AM Apr 7, 2015
 */
public abstract class CloudPipeBase {

    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final Logger log = Logger.getLogger("cloudPipe");
    protected static final String zkBase = "/config/kafka_clusters";
    
    private Producer<String, String> producer;
    private String cluster;
    private String biz;
    
    private void fetchClusterMetadata(ZooKeeper zk) throws Exception {
        List<String> clusters = zk.getChildren(zkBase, false);
        for (String c : clusters) {
            List<String> bizs = zk.getChildren(zkBase + '/' + c, false);
            if (bizs.contains(biz)) {
                this.cluster = c;
                break;
            }
        }
    }
    
    private String fetchClusterBrokers() {
        try {
            ZooKeeper zk = new ZooKeeper(zkCommEndpoint(), 3000, null);
            fetchClusterMetadata(zk);
            String path = zkBase + '/' + cluster;
            byte[] bs = zk.getData(path, false, new Stat());
            List<String> bizs = zk.getChildren(path, false);
            if (bizs == null || !bizs.contains(biz)) {
                throw new Exception("biz_not_belongs_to_cluster:" + biz);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> obj = mapper.readValue(new String(bs), Map.class);
            String brokers = (String) obj.get("brokers");
            zk.close();
            return brokers;
        } catch (Exception e) {
            log.error("fetchClusterBrokers_failed:", e);
        }
        return null;
    }

    protected CloudPipeBase() {
        init();
    }
    
    protected void init() {
        this.biz = getBiz();
        Properties props = null;
        try {
            props = prepareKafkaParams();
        } catch (RuntimeException e) {
            return;
        }
        producer = new Producer<String, String>(new ProducerConfig(props));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (producer != null) producer.close();
            }
        });
    }

    protected void pumpString(String msg) {
        producer.send(new KeyedMessage<String, String>(
            biz, String.valueOf(Math.random()), msg
        ));
    }
    
    protected void pumpMap(Map<String, Object> obj) {
        try {
            obj.put("ts", System.currentTimeMillis());
            String msg = mapper.writeValueAsString(obj);
            pumpString(msg);
        } catch (Exception e) {
            log.error("CloudPipe_failed:", e);
        }
    }
    
    protected abstract String getBiz();
    
    protected Properties prepareKafkaParams() {
        Properties props = new Properties();
        String clusterBrokers = fetchClusterBrokers();
        if (clusterBrokers == null) {
            throw new RuntimeException("connect kafka fail");
        }
        props.put("metadata.broker.list", clusterBrokers);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "0");
        props.put("producer.type", "sync");
        props.put("queue.enqueue.timeout.ms", "-1");
        props.put("batch.num.messages", "200");
        props.put("compression.codec", "1");
        return props;
    }
    
    protected String zkCommEndpoint() {
        return "std-1.zk.infra.duitang.net:3881,std-2.zk.infra.duitang.net:3881,std-3.zk.infra.duitang.net:3881";
    }
}
