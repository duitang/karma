package com.duitang.service.base;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

public class MetricCenter {

	final public static MetricRegistry metrics = new MetricRegistry();
	final public static JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
	static {
		reporter.start();
	}

}
