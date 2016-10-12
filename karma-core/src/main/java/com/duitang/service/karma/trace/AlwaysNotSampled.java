package com.duitang.service.karma.trace;

public class AlwaysNotSampled implements TracerSampler {

	@Override
	public boolean sample(String clazzName, String method, Object[] params) {
		return false;
	}

	@Override
	public boolean sample() {
		return true;
	}

}
