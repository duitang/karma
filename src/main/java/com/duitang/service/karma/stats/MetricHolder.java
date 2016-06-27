package com.duitang.service.karma.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetricHolder {
  public volatile List<Map> holding = new ArrayList<>();

  public Reporter reporter() {
    return new Reporter() {
      @Override
      public void report(List<Map> data) throws Exception {
        holding = data;
      }
    };
  }
}
