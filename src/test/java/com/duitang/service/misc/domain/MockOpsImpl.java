package com.duitang.service.misc.domain;

import java.util.HashMap;

public class MockOpsImpl implements MockOps {

	protected HashMap<String, MockObject> repo = new HashMap<String, MockObject>();

	@Override
	public MockObject getMock(String name) {
		return repo.get(name);
	}

	@Override
	public void setMock(String name, MockObject obj) {
		repo.put(name, obj);
	}

}
