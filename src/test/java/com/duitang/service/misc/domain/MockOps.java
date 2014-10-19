package com.duitang.service.misc.domain;

public interface MockOps {

	MockObject getMock(String name);

	void setMock(String name, MockObject obj);
	
}
