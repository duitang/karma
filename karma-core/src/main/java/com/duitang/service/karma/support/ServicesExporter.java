package com.duitang.service.karma.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.boot.ServerBootstrap;
import com.google.common.collect.Lists;

/**
 * Magic starts from here
 * 
 * <pre>
 * notion, fix by laurence: 
 * a. reduce 1 interface limit
 * b. like ip-tables using
 * 	  1. disable policy
 * 	  2. enable policy
 * </pre>
 *
 * @author kevx
 * @author laurence
 * @since 12/10/2014
 */
public class ServicesExporter {

	private List<Object> services;
	private List<String> exportedInterfaces = Lists.newArrayList();

	protected List<String> enabledPt = null;
	protected List<String> disabledPt = Arrays.asList("^java");

	protected List<Pattern> ePt;
	protected List<Pattern> dPt;

	private int port;
	private int maxQueuingLatency = 500;
	private ServerBootstrap boot;

	private final Logger log = LoggerFactory.getLogger("server");

	public void init() {
		List<Pattern> r = null;
		r = new ArrayList<>();
		if (enabledPt != null) {
			for (String p : enabledPt) {
				r.add(Pattern.compile(p));
			}
		}
		this.ePt = r;
		r = new ArrayList<>();
		if (disabledPt != null) {
			for (String p : disabledPt) {
				r.add(Pattern.compile(p));
			}
		}
		this.dPt = r;
		try {
			boot = new ServerBootstrap();
			for (Object svc : services) {
				Class<?>[] allIntfce = svc.getClass().getInterfaces();
				for (Class<?> iface : allIntfce) {
					if (onlineInterface(iface.getName())) {
						exportedInterfaces.add(iface.getName());
						boot.addService(iface, svc);
						boot.setMaxQueuingLatency(maxQueuingLatency);
						log.warn("ServicesExporter_inited:" + iface.getName());
					}
				}
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

	public void setMaxQueuingLatency(int maxQueuingLatency) {
		this.maxQueuingLatency = maxQueuingLatency;
	}

	public List<String> getExportedInterfaces() {
		return exportedInterfaces;
	}

	public List<String> getEnabledPt() {
		return enabledPt;
	}

	public void setEnabledPt(List<String> enabledPt) {
		this.enabledPt = enabledPt;
	}

	public List<String> getDisabledPt() {
		return disabledPt;
	}

	public void setDisabledPt(List<String> disabledPt) {
		this.disabledPt = disabledPt;
	}

	protected boolean onlineInterface(String pkg) {
		boolean useEnabled = enabledPt == null ? false : true;
		boolean useDisabled = disabledPt == null ? false : true;

		boolean hitDisabled = false;
		// 1. check disabled
		for (Pattern pp : dPt) {
			hitDisabled = !useDisabled ? false : pp.matcher(pkg).find();
			if (hitDisabled) {
				return false; // fast hit disabled
			}
		}

		boolean hitEnabled = false;
		// 2. check enable
		for (Pattern pp : ePt) {
			hitEnabled = !useEnabled ? true : pp.matcher(pkg).find();
			if (hitEnabled) {
				return true;
			}
		}

		// 3. when useEnabled --> hitEnabled ; otherwise enable all
		return useEnabled ? hitEnabled : true;
	}

}
