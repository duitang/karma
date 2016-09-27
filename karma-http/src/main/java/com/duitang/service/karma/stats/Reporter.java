package com.duitang.service.karma.stats;

import java.util.List;
import java.util.Map;

public interface Reporter {
  void report(List<Map> data) throws Exception;
}
