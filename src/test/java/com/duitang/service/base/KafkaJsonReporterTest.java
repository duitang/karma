package com.duitang.service.base;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import org.apache.avro.AvroRemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.demo.DemoService;
import com.duitang.service.demo.MemoryCacheClientFactory;
import com.duitang.service.demo.MemoryCacheService;

public class KafkaJsonReporterTest {

	ServerBootstrap boot = null;
	MemoryCacheClientFactory fac = null;

	@Before
	public void setUp() throws Exception {
		MemoryCacheService impl = new MemoryCacheService();
		boot = new ServerBootstrap();
		try {
			boot.startUp(DemoService.class, impl, 9090);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		fac = new MemoryCacheClientFactory();
		fac.setUrl("http://127.0.0.1:9090");
	}

	@After
	public void tearDown() throws Exception {
		boot.shutdown();
	}

//	@Test
	public void test() {
		Properties props = new Properties();
		props.put("zookeeper.connect", "192.168.172.2:2181");
		props.put("metadata.broker.list", "192.168.172.2:9092");
		props.put("group.id", "my");
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");

		MetricCenter.enableKafkaReporter(props, 1);
		DemoService cli = fac.create();
		for (int i = 0; i < 10; i++) {
			try {
				System.out.println(cli.trace_msg("wait_500", 100));
			} catch (AvroRemoteException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < 5; i++) {
			try {
				System.out.println(cli.trace_msg("wait_500", 600));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		fac.release(cli);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void test2() {
		Properties props = new Properties();
		props.put("zookeeper.connect", "192.168.172.2:2181");
		props.put("metadata.broker.list", "192.168.172.2:9092");
		props.put("group.id", "my");
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");

		String topic = KafkaJsonReporter.METRICS_NAME;
		ConsumerConfig cfg = new ConsumerConfig(props);
		ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(cfg);

		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, new Integer(1));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> stream = consumerMap.get(topic);

		System.out.println("Total Kafka Stream: " + stream.size());

		String msg;
		for (KafkaStream<byte[], byte[]> ks : stream) {
			msg = new String(ks.iterator().next().message());
			System.out.println("=================================" + msg);
		}
	}

}
