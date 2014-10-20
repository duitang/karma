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

	@Test
	public void test() {
		Properties props = new Properties();
		// props.put("zookeeper.connect", "192.168.172.2:2181");
		props.put("metadata.broker.list", "192.168.172.2:9092");
		props.put("group.id", "my");
		// props.put("batch.num.messages", "1");
		// props.put("compression.codec", "gzip");
		// props.put("request.required.acks", "0");
		// props.put("producer.type", "async");
		// props.put("queue.enqueue.timeout.ms", "-1");
		// props.put("queue.buffering.max.ms", "5000");
		// props.put("queue.buffering.max.messages", "10000");
		// props.put("queue.enqueue.timeout.ms", "20000");
		// props.put("message.send.max.retries", "2");

		MetricCenter.enableKafkaReporter(props, 1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		DemoService cli = fac.create();
		for (int i = 0; i < 10; i++) {
			try {
				System.out.println(cli.trace_msg("wait_500", 100));
			} catch (Exception e) {
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
		// props.put("metadata.broker.list", "192.168.172.2:9092");
		props.put("group.id", "my");
		props.put("zookeeper.session.timeout.ms", "400");
		props.put("zookeeper.sync.time.ms", "200");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.commit", "true");

		String topic = KafkaJsonReporter.METRICS_QUEUE_NAME;
		ConsumerConfig cfg = new ConsumerConfig(props);
		ConsumerConnector consumer = kafka.consumer.Consumer.createJavaConsumerConnector(cfg);

		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, new Integer(1));
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);

		while (true) {
			List<KafkaStream<byte[], byte[]>> stream = consumerMap.get(topic);

			System.out.println("Total Kafka Stream: " + stream.size());

			String msg;
			for (KafkaStream<byte[], byte[]> ks : stream) {
				msg = new String(ks.iterator().next().message());
				System.out.println("=================================" + msg);
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
