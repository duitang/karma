package com.duitang.service.karma.demo.domain;

import com.duitang.service.karma.demo.DemoObject;

import java.util.List;
import java.util.Map;

public interface Demo3 {

  DemoObject m1(DemoObject obj);

  List<DemoObject> m2(List<DemoObject> obj);

  Map<String, DemoObject> m3(Map<String, DemoObject> objs);

  Map<String, Float> m4(Map<String, Long> objs);

}
