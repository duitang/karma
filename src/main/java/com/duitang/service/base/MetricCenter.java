package com.duitang.service.base;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class MetricCenter {

	final public static boolean debug = false;
	final public static MetricRegistry metrics = new MetricRegistry();
//	final public static JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
	public static ConsoleReporter console;
	public static KafkaJsonReporter kafkaReporter;
	static {
//		reporter.start();
	}
	public static Map<String, Meter> method_qps = new HashMap<String, Meter>();
	public static Map<String, Histogram> method_dur = new HashMap<String, Histogram>();

	public static void methodMetric(String name, long startts) {
		methodMetric(name, startts, false);
	}

	public static void methodMetric(String name, long startts, boolean failure) {
		if (failure) {
			name += ":Failure";
		}
		Histogram hist = MetricCenter.method_dur.get(name);
		if (hist != null) {
			hist.update(System.currentTimeMillis() - startts);
		}
		Meter m = MetricCenter.method_qps.get(name);
		if (m != null) {
			m.mark();
		}
	}

	static public String getHostname() {
		String ret;
		try {
			ret = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			ret = System.getenv("HOSTNAME");
		}
		return ret;
	}

	@SuppressWarnings("static-access")
	public static void initMetric(Class clazz, String clientid) {
		String nm;
		String nmf;
		for (Method m : clazz.getMethods()) {
			nm = clientid + ":" + m.getName();
			nmf = nm + ":Failure";
			if (debug) {
				System.err.println(" ------> " + nm);
			}
			// maybe racing, but not serious problem
			if (!MetricCenter.method_qps.containsKey(nm)) {
				MetricCenter.method_qps.put(nm, MetricCenter.metrics.meter(MetricCenter.metrics.name(nm + ":qps")));
			}
			// maybe racing, but not serious problem
			if (!MetricCenter.method_dur.containsKey(nm)) {
				MetricCenter.method_dur.put(nm,
				        MetricCenter.metrics.histogram(MetricCenter.metrics.name(nm + ":resp_time")));
			}
			// maybe racing, but not serious problem
			if (!MetricCenter.method_qps.containsKey(nmf)) {
				MetricCenter.method_qps.put(nmf, MetricCenter.metrics.meter(MetricCenter.metrics.name(nmf + ":qps")));
			}
			// maybe racing, but not serious problem
			if (!MetricCenter.method_dur.containsKey(nmf)) {
				MetricCenter.method_dur.put(nmf,
				        MetricCenter.metrics.histogram(MetricCenter.metrics.name(nmf + ":resp_time")));
			}
		}
	}

	static public void enableConsoleReporter(int peroid) {
		ConsoleReporter ret = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS)
		        .convertDurationsTo(TimeUnit.MILLISECONDS).build();
		ret.start(peroid, TimeUnit.SECONDS);
		console = ret;
	}

	static public void enableKafkaReporter(Properties config, long peroid) {
		kafkaReporter = new KafkaJsonReporter(metrics, "kafkaJsonReporter", MetricFilter.ALL, TimeUnit.SECONDS,
		        TimeUnit.MILLISECONDS, config);
		kafkaReporter.start(peroid, TimeUnit.SECONDS);
	}
}
