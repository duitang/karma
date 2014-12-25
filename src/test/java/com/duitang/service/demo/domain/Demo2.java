package com.duitang.service.demo.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Demo2 {

	Map<String, Long> m1(Map<String, Number> data);

	List<String> m2(List<Float> data);

	Set<Float> m3(List<Integer> data);

	Double[] m4(List<Boolean> data);

}
