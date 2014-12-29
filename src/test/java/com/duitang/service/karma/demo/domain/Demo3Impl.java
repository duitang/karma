package com.duitang.service.karma.demo.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.duitang.service.karma.demo.DemoObject;

public class Demo3Impl implements Demo3 {

	@Override
	public DemoObject m1(DemoObject obj) {
		DemoObject ret = new DemoObject();
		ret.setB_v(obj.isB_v() && true);
		ret.setBs_v("shit".getBytes());
		ret.setF_v(obj.getF_v() + 1.1f);
		ret.setI_v(obj.getI_v() + 2);
		ret.setL_v(obj.getL_v() + 11);
		ret.setM_v(new HashMap(obj.getM_v()));
		ret.getM_v().put("happy", "new year");
		return ret;
	}

	@Override
	public List<DemoObject> m2(List<DemoObject> obj) {
		ArrayList<DemoObject> ret = new ArrayList<DemoObject>(obj);
		return ret;
	}

	@Override
	public Map<String, DemoObject> m3(Map<String, DemoObject> objs) {
		for (DemoObject o : objs.values()) {
			o.setDomain("fuck");
			o.setMethod("u");
		}
		return objs;
	}

	@Override
	public Map<String, Float> m4(Map<String, Long> objs) {
		Map<String, Float> ret = new HashMap<String, Float>();
		Set<Entry<String, Long>> sss = objs.entrySet();
		for (Entry<String, Long> oo : sss) {
			ret.put(oo.getKey(), oo.getValue().floatValue());
		}
		return ret;
	}

}
