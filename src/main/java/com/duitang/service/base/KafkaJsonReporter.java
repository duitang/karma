package com.duitang.service.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaJsonReporter extends ScheduledReporter {

	final static public String METRICS_QUEUE_NAME = "Service_Metrics";
	protected boolean sample = true;
	protected static ObjectMapper mapper;
	protected Producer<String, String> reportServer;
	protected Properties config;

	protected void init() {
		mapper = new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.MILLISECONDS, sample));
		config.put("serializer.class", "kafka.serializer.StringEncoder");
		config.put("request.required.acks", "0");
		config.put("batch.num.messages", "100");
		config.put("compression.codec", "gzip");
		config.put("queue.buffering.max.ms", "5000");
		config.put("queue.buffering.max.messages", "10000");
		config.put("queue.enqueue.timeout.ms", "20000");
		config.put("message.send.max.retries", "2");
		ProducerConfig prodconf = new ProducerConfig(config);
		reportServer = new Producer<String, String>(prodconf);
	}

	protected KafkaJsonReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
	        TimeUnit durationUnit) {
		this(registry, name, filter, rateUnit, durationUnit, new Properties());
	}

	public KafkaJsonReporter(MetricRegistry registry, String name, MetricFilter filter, TimeUnit rateUnit,
	        TimeUnit durationUnit, Properties props) {
		super(registry, name, filter, rateUnit, durationUnit);
		config = props;
		init();
	}

	@Override
	public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
	        SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		Map<String, String> ret = new HashMap<String, String>();
		try {
			ret.put("gauges", mapper.writeValueAsString(gauges));
		} catch (JsonProcessingException e) {
		}
		try {
			ret.put("counters", mapper.writeValueAsString(counters));
		} catch (JsonProcessingException e) {
		}
		try {
			ret.put("histograms", mapper.writeValueAsString(histograms));
		} catch (JsonProcessingException e) {
		}
		try {
			ret.put("meters", mapper.writeValueAsString(meters));
		} catch (JsonProcessingException e) {
		}
		try {
			ret.put("timers", mapper.writeValueAsString(timers));
		} catch (JsonProcessingException e) {
		}
		ret.put("hostname", MetricCenter.getHostname());
		try {
			KeyedMessage<String, String> data = new KeyedMessage<String, String>(METRICS_QUEUE_NAME,
			        MetricCenter.getHostname(), mapper.writeValueAsString(ret));
			reportServer.send(data);
		} catch (JsonProcessingException e) {
		}
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
}
