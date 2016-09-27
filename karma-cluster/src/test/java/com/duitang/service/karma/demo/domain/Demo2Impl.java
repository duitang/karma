package com.duitang.service.karma.demo.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Demo2Impl implements Demo2 {

  @Override
  public Map<String, Long> m1(Map<String, Number> data) {
    Map<String, Long> ret = new HashMap<String, Long>();
    for (Entry<String, Number> en : data.entrySet()) {
      ret.put(en.getKey(), en.getValue().longValue());
    }
    return ret;
  }

  @Override
  public List<String> m2(List<Float> data) {
    ArrayList<String> ret = new ArrayList<String>();
    for (Float d : data) {
      ret.add(d.toString());
    }
    return ret;
  }

  @Override
  public Set<Float> m3(List<Integer> data) {
    HashSet<Float> ret = new HashSet<Float>();
    for (Integer d : data) {
      ret.add(d.floatValue());
    }
    return ret;
  }

  @Override
  public Double[] m4(List<Boolean> data) {
    Double[] ret = new Double[data.size()];
    for (int i = 0; i < data.size(); i++) {
      ret[i] = data.get(i) ? 1.0d : 0;
    }
    return ret;
  }

}
