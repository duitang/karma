package com.duitang.service.karma.server;

import java.util.HashMap;
import java.util.Map;

public class ServiceConfig {

	protected Map<Class, Object> services;

	public void addService(Class iface, Object impl) {
		if (services == null) {
			services = new HashMap<Class, Object>();
		}
		services.put(iface, impl);
	}

	public void setServices(Map<Class, Object> serv) {
		this.services = serv;
	}

	public Map<Class, Object> getServices() {
		return this.services;
	}

}
