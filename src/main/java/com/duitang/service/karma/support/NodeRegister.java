package com.duitang.service.karma.support;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 服务自动发现机制
 * 
 * 每台机器启动后均将自身注册到zk中；
 * 当机器失联后zk会自动删除相应节点
 * 
 * 对于服务提供者还会将ServiceExporter暴露的接口写入ZK
 * 
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String now = sdf.format(new Date());
		Map<String, String> m = Maps.newHashMap();
		m.put("rpc_gmt_create", now);
		if (servicesExporter != null) {
		    //this is a service provider
    		List<String> all = servicesExporter.getExportedInterfaces();
    		if (all.size() > 0) {
    			m.put("rpc_interfaces", Joiner.on(';').join(all));
    			m.put("rpc_port", String.valueOf(servicesExporter.getPort()));
    		}
		}
		return mapper.writeValueAsString(m);
	}
	
	public boolean isDev() {
		try {
			InetAddress ia = InetAddress.getLocalHost();
			if (IpRanges.isProduction(ia.getHostAddress())) {
			    return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public void run() {
		try {
		    Validate.notBlank(appName);
			String cs = connString;
			if (isDev()) {
				cs = connStringDev;
			}
			while (true) {
				Thread.sleep(1000);
				if (zk == null || !zk.getState().isAlive()) {
				    if (zk != null) {
				        zk.close();
				    }
					resetZk(cs);
				}
			}
		} catch (Exception e) {
			log.error("NodeRegister_failed:", e);
		}
	}

	private void resetZk(String cs) throws Exception {
	    int tries = 0;
		try {
            InetAddress ia = InetAddress.getLocalHost();
            String host = ia.getHostAddress();
            String data = makeData();
            ACL acl = new ACL(Perms.ALL, Ids.ANYONE_ID_UNSAFE);
            if (zk != null) zk.close();
            zk = new ZooKeeper(cs, 3000, this);
            while (zk.getState() != States.CONNECTED && tries < 100) {
                //waiting for zk initialization
                tries++;
                Thread.sleep(500);
            }
            zk.create(
            	"/app/" + appName + '/' + host, 
            	data.getBytes(), 
            	Lists.newArrayList(acl), 
            	CreateMode.EPHEMERAL
            );
        } catch (Exception e) {
            if (zk != null) zk.close();//close it right now
            log.error("NodeRegister_resetZk_failed:" + tries, e);
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
