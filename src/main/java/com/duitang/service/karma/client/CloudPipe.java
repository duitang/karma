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
 * 配置以下项
 * zkCommEndpoint：公共zk集群
 * biz：消息业务名
 * 
 * @author kevx
 * @since 12:01:27 PM Mar 12, 2015
 */
public class CloudPipe {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = Logger.getLogger("cloudPipe");
    private static final String zkBase = "/config/kafka_clusters";
    
    private Producer<String, String> producer;
    private String zkCommEndpoint;
    private String cluster;
    private String biz;
    
    public void setZkCommEndpoint(String zkCommEndpoint) {
        this.zkCommEndpoint = zkCommEndpoint;
    }

    public void setBiz(String biz) {
        this.biz = biz;
    }

    public static enum ExceptionSource {
        VIEW,
        DOMAIN,
        RPC,
        DAO,
        UTIL,
        EXT;
    }
    
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
            ZooKeeper zk = new ZooKeeper(zkCommEndpoint, 3000, null);
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

    private void init() {
        Properties props = new Properties();
        props.put("metadata.broker.list", fetchClusterBrokers());//
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "0");
        props.put("producer.type", "sync");
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

    private void pumpException(ExceptionSource source, Throwable detail) {
        if (source == null || detail == null) return;
        Map<String, Object> m = Maps.newHashMap();
        m.put("source", source.name().toLowerCase());
        m.put("type", detail.getClass().getName());
        m.put("message", detail.getMessage());
        m.put("top_frame", "");
        if (detail.getStackTrace() != null && detail.getStackTrace().length > 0) {
            m.put("top_frame", detail.getStackTrace()[0].getClassName());
        }
        pump(m);
    }
    
    public static void exception(ExceptionSource source, Throwable detail) {
        CloudPipeInsts.app_exception.inst.pumpException(source, detail);
    }
    
    public void stat(String name, String node, String category, Object value) {
        CloudPipeInsts.app_stat.inst.pumpStat(name, node, category, value);
    }
    
    public static void networkProbe(String str) {
        CloudPipeInsts.app_network_probe.inst.pump(str);
    }
    
    public static void raw(String rawdata) {
        CloudPipeInsts.app_raw.inst.pump(rawdata);
    }
    
    private void pumpStat(String name, String node, String category, Object value) {
        Map<String, Object> m = Maps.newHashMap();
        m.put("name", name);
        m.put("node", node);
        m.put("category", category);
        if (value != null) {
            if (value instanceof Number) {
                m.put("value", ((Number)value));
            } else {
                m.put("value", value.toString());
            }
        } else {
            m.put("value", "");
        }
        pump(m);
    }
    
    private void pump(Map<String, Object> obj) {
        try {
            obj.put("ts", System.currentTimeMillis());
            String msg = mapper.writeValueAsString(obj);
            pump(msg);
        } catch (Exception e) {
            log.error("CloudPipe_failed:", e);
        }
    }

    private void pump(String msg) {
        producer.send(new KeyedMessage<String, String>(
            biz, String.valueOf(Math.random()), msg
        ));
    }
    
    public static void main(String[] args) throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        for (int i = 0; i < 1000; i++) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("val1", Math.random());
            data.put("val2", new java.util.Date().toString());
            data.put("val3", "qwerty");
            String str = mapper.writeValueAsString(data);
            CloudPipe.networkProbe(str);
            Thread.sleep(2);
        }
        sw.stop();
        Thread.sleep(1000);
        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));
        Runtime.getRuntime().exit(0);
    }
    
    private static enum CloudPipeInsts {
        app_exception,
        app_stat,
        app_raw,
        app_network_probe
        ;
        
        public final CloudPipe inst;
        private CloudPipeInsts() {
            CloudPipe cp = new CloudPipe();
            cp.setBiz(this.name().toLowerCase());
            cp.setZkCommEndpoint("s44:3881,s45:3881,s46:3881");
            cp.init();
            inst = cp;
        }
    }
}
