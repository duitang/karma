package com.duitang.service.karma.demo;

import java.util.List;

public class DemoJsonRPCImpl implements DemoJsonRPCService {

  @Override
  public DemoRPCDTO getObject0(DemoRPCDTO obj, List<DemoRPCDTO> lst, Long id, String name, List<Float> score) {
    DemoRPCDTO ret = obj;
    for (DemoRPCDTO o : lst) {
      ret.setA(ret.getA() + o.getA());
      ret.getB().addAll(o.getB());
      ret.getC().putAll(o.getC());
    }
    String k = id + name;
    Float v = 0f;
    for (Float f : score) {
      v += f;
    }
    ret.getC().put(k, v.doubleValue());
    return ret;
  }

}
