package com.duitang.service.karma.stats;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import com.duitang.service.karma.base.MetricCenter;
import com.duitang.service.karma.pipe.CloudPipeBase;

public class DWMetricReporter extends CloudPipeBase implements CustomDataReporter {
    public static String BIZ = "dw30_metrics";
    public final static ObjectMapper DWMAPPER = new ObjectMapper().registerModule(new MetricsModule(
            TimeUnit.SECONDS, // rate unit
            TimeUnit.SECONDS, // duration unit
            true, // show samples
            MetricFilter.ALL));

    @Override
    protected String getBiz() {
        return BIZ;
    }

    @Override
    public void report() throws Exception {
        pumpString(genReport());
    }

    public String genReport() throws JsonProcessingException {
        return DWMAPPER.writeValueAsString(
                ImmutableMap.of(
                        "timestamp", String.valueOf(System.currentTimeMillis()),
                        "location", MetricCenter.getLocation(),
                        "metrics", DropwizardMetricHolder.getRegistry()
                )
        );
    }
}