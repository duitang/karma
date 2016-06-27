package com.duitang.service.karma.pipe;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * @author kevx
 * @since 10:49:58 AM Apr 7, 2015
 */
public abstract class CloudPipeBase {

  protected static final ObjectMapper mapper = new ObjectMapper();
  protected static final Logger log = Logger.getLogger("cloudPipe");
  protected static final String zkBase = "/config/kafka_clusters";

  private Producer<String, String> producer;
  private String clusterBrokers;
  private Long updated;

  protected CloudPipeBase() {
    createProducer();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        if (producer != null) producer.close();
      }
    });
  }

  public static void main(String[] args) {
    CloudPipeBase c = new CloudPipeBase() {
      @Override
      protected String getBiz() {
        return "karma_metrics";
      }
    };
    c.pumpString(null);
  }

  private String fetchClusterMetadata(ZooKeeper zk) throws Exception {
    List<String> clusters = zk.getChildren(zkBase, false);
    for (String c : clusters) {
      List<String> bizs = zk.getChildren(zkBase + '/' + c, false);
      if (bizs.contains(getBiz())) {
        return c;
      }
    }
    return null;
  }

  private String fetchClusterBrokers() {
    ZooKeeper zk = null;
    try {
      zk = new ZooKeeper(zkCommEndpoint(), 3000, new Watcher() {
        @Override
        public void process(WatchedEvent event) {
          log.warn("got event" + event.toString());
        }
      });
      String meta = fetchClusterMetadata(zk);
      if (meta == null) {
        return null;
      }
      String path = zkBase + '/' + meta;
      byte[] bs = zk.getData(path, false, new Stat());
      List<String> bizs = zk.getChildren(path, false);
      if (bizs == null || !bizs.contains(getBiz())) {
        log.error("biz_not_belongs_to_cluster:" + getBiz());
        return null;
      }
      @SuppressWarnings("unchecked")
      Map<String, Object> obj = mapper.readValue(new String(bs), Map.class);
      return (String) obj.get("brokers");
    } catch (Exception e) {
      log.error("fetchClusterBrokers_failed:", e);
    } finally {
      try {
        if (zk != null) {
          zk.close();
        }
      } catch (InterruptedException ignored) {
      }
    }
    return null;
  }

  protected void createProducer() {
    Properties props;
    try {
      props = prepareKafkaParams();
    } catch (RuntimeException e) {
      return;
    }
    if (props == null) {
      return;
    }
    producer = new Producer<String, String>(new ProducerConfig(props));
  }

  protected void pumpString(String msg) {
    if (producer == null) {
      createProducer();
    }
    if (producer == null) {
      log.error(String.format("cannot use pipeline, biz: %s", getBiz()));
      return;
    }
    producer.send(new KeyedMessage<String, String>(
        getBiz(), String.valueOf(Math.random()), msg
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
    if (updated == null || System.currentTimeMillis() - updated > 1000 * 90) {
      clusterBrokers = fetchClusterBrokers();
      updated = System.currentTimeMillis();
    }
    Properties props = new Properties();
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
