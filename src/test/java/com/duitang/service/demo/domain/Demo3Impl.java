package com.duitang.service.demo.domain;

import java.util.HashMap;

import com.duitang.service.demo.DemoObject;

public class Demo3Impl implements Demo3 {

	@Override
	public DemoObject getObject(DemoObject obj) {
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

}
