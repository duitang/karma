package com.duitang.service.l2;

import com.duitang.service.base.ClientFactory;

public class L2ServiceFactory extends ClientFactory<L2Service> {

	protected final static String SERVICE_NAME = "L2Service";

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public Class getServiceType() {
		return L2Service.class;
	}

}
