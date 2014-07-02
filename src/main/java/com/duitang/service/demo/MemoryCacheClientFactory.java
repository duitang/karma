package com.duitang.service.demo;

import com.duitang.service.base.AbstractClientFactory;

public class MemoryCacheClientFactory extends AbstractClientFactory<DemoService> {

	final static String servicename = MemoryCacheService.class.getName();

	@Override
	public String getServiceName() {
		return servicename;
	}

	@Override
	public Class getServiceType() {
		return DemoService.class;
	}

}
