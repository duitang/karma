package com.duitang.service.karma.client;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

/**
 * 配置以下三项
 * zkCommEndpoint：公共zk集群
 * cluster：消息目标集群
 * biz：消息业务名
 * 
 * @author kevx
 * @since 12:01:27 PM Mar 12, 2015
 */
public class CloudPipe {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = Logger.getLogger("cloudPipe");
    
    private Producer<String, String> producer;
    private String zkCommEndpoint;
    private String cluster;
    private String biz;
    
    public void setZkCommEndpoint(String zkCommEndpoint) {
        this.zkCommEndpoint = zkCommEndpoint;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public String fetchClusterBrokers(String cluster) {
        try {
            String path = "/config/kafka_clusters/" + cluster;
            ZooKeeper zk = new ZooKeeper(zkCommEndpoint, 3000, null);
            byte[] bs = zk.getData(path, false, new Stat());
            List<String> bizs = zk.getChildren(path, false);
            zk.close();
            if (bizs == null || !bizs.contains(biz)) {
                throw new Exception("biz_not_belongs_to_cluster:" + biz);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> obj = mapper.readValue(new String(bs), Map.class);
            String brokers = (String) obj.get("brokers");
            return brokers;
        } catch (Exception e) {
            log.error("fetchClusterBrokers_failed:", e);
        }
        return null;
    }

    public void init() {
        Properties props = new Properties();
        props.put("metadata.broker.list", fetchClusterBrokers(cluster));
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "0");
        props.put("producer.type", "async");
        props.put("queue.enqueue.timeout.ms", "-1");
        props.put("batch.num.messages", "200");
        props.put("compression.codec", "1");
        producer = new Producer<String, String>(new ProducerConfig(props));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (producer != null) producer.close();
            }
        });
    }
    
    /**
     * 发送消息到消息集群
     */
    public void pump(Object obj) {
        try {
            String msg = mapper.writeValueAsString(obj);
            producer.send(new KeyedMessage<String, String>(biz, msg));
        } catch (Exception e) {
            log.error("CloudPipe_failed:", e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CloudPipe pipe = new CloudPipe();
        pipe.setBiz("test");
        pipe.setCluster("msg-dev");
        pipe.setZkCommEndpoint("s44:3881,s45:3881,s46:3881");
        pipe.init();
        Stopwatch sw = Stopwatch.createStarted();
        for (int i = 0; i < 50000; i++) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("val1", Math.random());
            data.put("val2", Math.random());
            data.put("val3", "qwerty");
            pipe.pump(data);
            Thread.sleep(1);
        }
        sw.stop();
        Thread.sleep(1000);
        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));
        Runtime.getRuntime().exit(0);
    }
}
