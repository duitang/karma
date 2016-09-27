package com.duitang.service.karma.stats;

import com.duitang.service.karma.base.MetricCenter;
import com.duitang.service.karma.pipe.CloudPipeBase;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.util.concurrent.TimeUnit;

public class DWMetricReporter extends CloudPipeBase implements CustomDataReporter {
  public static String BIZ = "dw30_metrics";
  public final static ObjectMapper DWMAPPER = new ObjectMapper().registerModule(new MetricsModule(
      TimeUnit.SECONDS, // rate unit
      TimeUnit.SECONDS, // duration unit
      false, // show samples
      MetricFilter.ALL));
  public final MetricRegistry registry;

  public DWMetricReporter() {
    this(DropwizardMetricHolder.getRegistry());
  }

  public DWMetricReporter(MetricRegistry registry) {
    super();
    this.registry = registry;
  }

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
            "timestamp", System.currentTimeMillis(),
            "instance", MetricCenter.getInstanceTag(),
            "metrics", registry
        )
    );
  }
}
