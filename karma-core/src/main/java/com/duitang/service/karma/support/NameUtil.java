package com.duitang.service.karma.support;

/**
 * MetricCenter.record("com.duitang.example.service.SomeService.methodName",
 * 20); // record in nanos
 */
public class NameUtil {

	final static String[] NOT_IN_PACKAGE_NAME = { "com.duitang.service.karma" };// "com.duitang.webx",

	/**
	 * HOSTNAME or randomly generated string
	 */
	static public String getHostname() {
		return InstanceTagHolder.getHostname();
	}

	static public void setAppName(String name) {
		if (name == null) {
			throw new NullPointerException("name==null");
		}
		InstanceTagHolder.setAppName(name);
		InstanceTagHolder.resetFinalTag();
	}

	static public void setHostname(String hostname) {
		if (hostname == null) {
			throw new NullPointerException("hostname==null");
		}
		InstanceTagHolder.setHostname(hostname);
		InstanceTagHolder.resetFinalTag();
	}

	static public InstanceTag getInstanceTag() {
		return InstanceTagHolder.INSTANCE_TAG;
	}

	public static String genClientIdFromCode() {
		StackTraceElement[] trac = Thread.currentThread().getStackTrace();
		String ret = "";
		String ss;
		boolean flag = false;
		for (int i = 3; i < trac.length; i++) {
			ret = trac[i].getClassName();
			ss = ret.toLowerCase();
			flag = false;
			for (String sss : NOT_IN_PACKAGE_NAME) {
				if (ret.startsWith(sss)) {
					flag = true;
					break;
				}
			}
			if (flag) {
				continue;
			}
			if (ss.contains("duitang")) {
				ret = trac[i].toString();
				break;
			}
		}
		return ret;
	}

}
