package com.duitang.service.karma.support;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 服务自动发现机制
 * 
 * 每台机器启动后均将自身注册到zk中
 * @author kevx
 * @since 5:57:54 PM Jan 13, 2015
 */
public class NodeRegister  implements Watcher, Runnable {

	private final static Logger log = Logger.getLogger(NodeRegister.class);
	private final static ObjectMapper mapper = new ObjectMapper();
	
	private String appName;
	private ZooKeeper zk;
	private String connString;
	private String connStringDev;
	
	private ServicesExporter servicesExporter;
	
	@Override
	public void process(WatchedEvent e) {
		//nothing to do
	}
	
	public void init() {
		new Thread(this, "ZkRegister").start();
	}
	
	public String makeData() throws Exception {
		List<String> all = servicesExporter.getExportedInterfaces();
		Map<String, String> m = Maps.newHashMap();
		if (all.size() > 0) {
			m.put("rpc_interfaces", Joiner.on(';').join(all));
			m.put("rpc_port", String.valueOf(servicesExporter.getPort()));
		}
		return mapper.writeValueAsString(m);
	}
	
	public boolean isDev() {
		try {
			InetAddress ia = InetAddress.getLocalHost();
			String ip = ia.getHostAddress();
			if (ip.startsWith("192.168.172.")) {
				String last = StringUtils.substringAfterLast(ip, ".");
				if (NumberUtils.toInt(last) >= 12) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public void run() {
		try {
			InetAddress ia = InetAddress.getLocalHost();
			String host = ia.getHostAddress();
			String data = makeData();
			String cs = connString;
			if (isDev()) {
				cs = connStringDev;
			}
			ACL acl = new ACL(Perms.ALL, Ids.ANYONE_ID_UNSAFE);
			zk = new ZooKeeper(cs, 3000, this);
			zk.create(
				"/app/" + appName + '/' + host, 
				data.getBytes(), 
				Lists.newArrayList(acl), 
				CreateMode.EPHEMERAL
			);
		} catch (Exception e) {
			log.error("NodeRegister_failed:", e);
		}
	}

	public void setConnString(String connString) {
		this.connString = connString;
	}

	public void setConnStringDev(String connStringDev) {
		this.connStringDev = connStringDev;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setServicesExporter(ServicesExporter servicesExporter) {
		this.servicesExporter = servicesExporter;
	}

}
