package com.duitang.service.karma.stats;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class DropwizardMetricHolder {
  public final static MetricRegistry registry = new MetricRegistry();

  static {
    registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
    registry.register("jvm.classes", new ClassLoadingGaugeSet());
    registry.register("jvm.memory", new MemoryUsageGaugeSet());
    registry.register("jvm.gc", new GarbageCollectorMetricSet());
    registry.register("jvm.threads", new ThreadStatesGaugeSet());
  }

  public static String genMetrics() throws IOException {
    return DWMetricReporter.DWMAPPER
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(registry);
  }

  public static MetricRegistry getRegistry() {
    return registry;
  }
}
