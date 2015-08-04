package com.duitang.service.karma.stats;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;

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
