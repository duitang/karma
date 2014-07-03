package com.duitang.service.base;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class MetricCenter {

	final public static MetricRegistry metrics = new MetricRegistry();
	final public static JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
	static {
		reporter.start();
	}
	public static Map<String, Meter> method_qps = new HashMap<String, Meter>();
	public static Map<String, Histogram> method_dur = new HashMap<String, Histogram>();

	public static void methodMetric(String name, long startts) {
		Histogram hist = MetricCenter.method_dur.get(name);
		if (hist != null) {
			hist.update(System.currentTimeMillis() - startts);
		}
		Meter m = MetricCenter.method_qps.get(name);
		if (m != null) {
			m.mark();
		}
	}

	@SuppressWarnings("static-access")
	public static void initMetric(Class clazz) {
		String nm;
		for (Method m : clazz.getMethods()) {
			nm = m.getName();
			// maybe racing, but not serious problem
			if (!MetricCenter.method_qps.containsKey(nm)) {
				MetricCenter.method_qps.put(nm,
				        MetricCenter.metrics.meter(MetricCenter.metrics.name(m.getName(), "qps")));
			}
			// maybe racing, but not serious problem
			if (!MetricCenter.method_dur.containsKey(nm)) {
				MetricCenter.method_dur.put(nm, MetricCenter.metrics.histogram(m.getName() + ":" + "response_time"));
			}
		}
	}

}
