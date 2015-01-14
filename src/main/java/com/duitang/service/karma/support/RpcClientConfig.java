package com.duitang.service.karma.support;

import org.apache.zookeeper.ZooKeeper;

/**
 * RPC配置
 * 
 * @author kevx
 * @since 5:27:23 PM Jan 13, 2015
 */
public class RpcClientConfig {

	private String appName;
	private String group;
	private String connString;
	private boolean usingStaticRpcEndpoint;
	private String staticRpcEndpoint;
	private ZooKeeper zk;
	public void init() {
		try {
			zk = new ZooKeeper(connString, 3000, null);
		} catch (Exception e) {
		}
	}
	
	public ZooKeeper getZk() {
		if (zk == null || !zk.getState().isAlive()) {
			init();
		}
		return zk;
	}

	public String getConnString() {
		return connString;
	}

	public void setConnString(String connString) {
		this.connString = connString;
	}

	public boolean isUsingStaticRpcEndpoint() {
		return usingStaticRpcEndpoint;
	}

	public void setUsingStaticRpcEndpoint(boolean usingStaticRpcEndpoint) {
		this.usingStaticRpcEndpoint = usingStaticRpcEndpoint;
	}

	public String getStaticRpcEndpoint() {
		return staticRpcEndpoint;
	}

	public void setStaticRpcEndpoint(String staticRpcEndpoint) {
		this.staticRpcEndpoint = staticRpcEndpoint;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

}
