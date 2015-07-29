package com.duitang.service.karma.stats;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.jvm.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JvmMetricHolder {
    public final static MetricRegistry registry = new MetricRegistry();

    public final static ObjectMapper mapper = new ObjectMapper().registerModule(new MetricsModule(
            TimeUnit.SECONDS, // rate unit
            TimeUnit.SECONDS, // duration unit
            true, // show samples
            MetricFilter.ALL));

    static {
        registry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        registry.register("jvm.classes", new ClassLoadingGaugeSet());
        registry.register("jvm.memory", new MemoryUsageGaugeSet());
        registry.register("jvm.gc", new GarbageCollectorMetricSet());
        registry.register("jvm.threads", new ThreadStatesGaugeSet());
    }

    public static String genMetrics() throws IOException {
        return mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(registry);
    }
}
