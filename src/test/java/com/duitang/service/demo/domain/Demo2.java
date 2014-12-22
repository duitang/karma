package com.duitang.service.demo.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Demo2 {

	Map<String, Long> m1(Map<String, Float> data);

	List<String> m2(ArrayList<Float> data);

	Set<Float> m3(Set<Integer> data);

	Double[] m4(String[] data);

}
