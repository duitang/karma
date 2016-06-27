package com.duitang.service.karma.stats;

import com.duitang.service.karma.pipe.CloudPipeBase;

import java.util.List;
import java.util.Map;

public class KafkaReporter extends CloudPipeBase implements Reporter {
  @Override
  protected String getBiz() {
    return "karma_metrics";
  }

  @Override
  public void report(List<Map> data) throws Exception {
    for (Map m : data) {
      pumpString(mapper.writeValueAsString(m));
    }
  }
}
