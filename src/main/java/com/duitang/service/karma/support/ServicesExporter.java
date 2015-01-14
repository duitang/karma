package com.duitang.service.karma.support;

import java.util.List;

import org.apache.log4j.Logger;

import com.duitang.service.karma.base.ServerBootstrap;


/**
 * Magic starts from here
 * 
 * @author kevx
 * @since 12/10/2014
 */
public class ServicesExporter {

	private List<Object> services;
	private int port;
	private ServerBootstrap boot;
	
	private final Logger log = Logger.getLogger("server");
	
	public void init() {
		try {
			boot = new ServerBootstrap();
			for (Object svc : services) {
				//this should never happen
				Class<?>[] allIntfce = svc.getClass().getInterfaces();
				Class<?> itf = null;
				for (Class<?> intfce : allIntfce) {
					if (intfce.getSimpleName().startsWith("I")) {
						itf = intfce;
					}
				}
				boot.addService(itf, svc);
				log.warn("ServicesExporter_inited:" + itf.getName());
			}
			boot.startUp(port);
		} catch (Exception e) {
			log.error("ServicesExporter::init_failed:", e);
		}
	}
	
	public void halt() {
		if (boot != null) {
			boot.shutdown();
		}
	}

	public List<Object> getServices() {
		return services;
	}

	public void setServices(List<Object> services) {
		this.services = services;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}
