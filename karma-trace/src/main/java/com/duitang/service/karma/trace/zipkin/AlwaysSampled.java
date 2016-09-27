package com.duitang.service.karma.trace.zipkin;

import com.duitang.service.karma.trace.TracerSampler;

public class AlwaysSampled implements TracerSampler {

	@Override
	public boolean sample(String clazzName, String method, Object[] params) {
		return true;
	}

}
