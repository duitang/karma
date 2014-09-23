package com.duitang.service.base;

import java.util.Map;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.codehaus.jackson.map.ObjectMapper;

public class KafkaJsonReporter implements Reporter {

	final static public String METRICS_QUEUE_NAME = "Service_Metrics";
	protected static ObjectMapper mapper;
	protected Producer<String, String> reportServer;
	protected Properties config;

	protected void init() {
		mapper = new ObjectMapper();
		config.put("serializer.class", "kafka.serializer.StringEncoder");
		config.put("request.required.acks", "0");
		config.put("batch.num.messages", "5");
		config.put("compression.codec", "gzip");
		config.put("queue.buffering.max.ms", "5000");
		config.put("queue.buffering.max.messages", "10000");
		config.put("queue.enqueue.timeout.ms", "20000");
		config.put("message.send.max.retries", "2");
		ProducerConfig prodconf = new ProducerConfig(config);
		reportServer = new Producer<String, String>(prodconf);
	}

	public KafkaJsonReporter(Properties props) {
		config = props;
		init();
	}

	static protected String getBackTraceName() {
		String ret = "";
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[5];
		if (e.getMethodName() != null) {
			ret = e.getFileName() + "@" + e.getLineNumber() + ":" + e.getMethodName();
		}
		return ret;
	}

	@Override
	public void report(Map data) {
		KeyedMessage<String, String> d;
		try {
			d = new KeyedMessage<String, String>(METRICS_QUEUE_NAME, MetricCenter.getHostname(),
			        mapper.writeValueAsString(data));
			reportServer.send(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
