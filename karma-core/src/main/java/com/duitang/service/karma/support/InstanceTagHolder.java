package com.duitang.service.karma.support;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.SystemPropertyUtil;

public class InstanceTagHolder {

	private static final Logger logger = LoggerFactory.getLogger(InstanceTagHolder.class);

	public static volatile InstanceTag INSTANCE_TAG;
	static String HOSTNAME = genHostName();
	private static String APP_NAME = genAppName();
	private static long PID = genPID();

	private static long genPID() {
		long pid = -1;
		try {
			String name = ManagementFactory.getRuntimeMXBean().getName();
			String[] split = name.split("@", 2);
			if (split.length > 1) {
				pid = Long.parseLong(split[0]);
			}
		} catch (Exception e) {
			logger.error("get pid error", e);
		}
		return pid;
	}

	private static String genHostName() {
		String name = null;
		try {
			name = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("get hostname error", e);
		}
		return name == null ? "" : name;
	}

	private static String genAppName() {
		String _appName = null;
		try {
			_appName = SystemPropertyUtil.get("app.name");
		} catch (Exception e) {
			logger.error("get APP_NAME error", e);
		}
		if (_appName == null){
			_appName = "UNKNOWN";
		}
		return _appName;
	}

	public static void setHostname(String hostname) {
		HOSTNAME = hostname;
		resetFinalTag();
	}

	public static void setAppName(String appName) {
		APP_NAME = appName;
		resetFinalTag();
	}

	public static String getHostname() {
		return HOSTNAME;
	}

	public static void resetFinalTag() {
		INSTANCE_TAG = new InstanceTag(APP_NAME, PID, HOSTNAME);
	}

	static {
		resetFinalTag();
	}
}
