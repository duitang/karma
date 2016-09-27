package com.duitang.service.karma.support;

public class InstanceTag {

	public final String app;
	public final long pid;
	public final String host;
	public final int ipv4;

	public InstanceTag(String app, long pid, String host) {
		this.app = app;
		this.pid = pid;
		this.host = host;
		ipv4 = IPUtils.getIPAsInt();
	}

}
