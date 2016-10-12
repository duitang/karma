package com.duitang.service.karma.trace;

import com.duitang.service.karma.trace.TracerSampler;

public class AlwaysSampled implements TracerSampler {

	@Override
	public boolean sample(String clazzName, String method, Object[] params) {
		return true;
	}

	@Override
	public boolean sample() {
		return true;
	}

}
