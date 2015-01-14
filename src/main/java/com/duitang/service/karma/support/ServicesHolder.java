package com.duitang.service.karma.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import com.duitang.service.karma.base.ClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

/**
 * 
 * @author kevx
 * @since 5:24:24 PM Jan 13, 2015
 */
public class ServicesHolder implements Watcher {
	
	private RpcClientConfig rpcClientConfig;
	
	private String interfaceName;
	private Class<Object> interfaceCls = null;
	private ClientFactory<Object> cf = null;
	private Timer timer;
	private static final String zkGroupBase = "/rpc_groups";
	
	private static final Logger log = Logger.getLogger("main");
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@SuppressWarnings("unchecked")
	public void init() {
		try {
			interfaceCls = (Class<Object>) Class.forName(interfaceName);
			createRpcStub();
		} catch (ClassNotFoundException e) {
			log.error("", e);
		}
	}
	
	public Object get() {
		return cf.create();
	}
	
	public Object create() {
		try {
			final Class<?> interfaceCls = Class.forName(interfaceName);
			return Proxy.newProxyInstance(
				ServicesHolder.class.getClassLoader(), 
				new Class<?>[]{interfaceCls}, 
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						Object rpc = get();
						try {
							return method.invoke(rpc, args);
						} finally {
							cf.release(rpc);
						}
						
					}
				}
			);
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	
	private List<String> getChildren(String path, boolean watch) {
		List<String> ret = null;
		try {
			ret = rpcClientConfig.getZk().getChildren(zkBase(), true);
			return ret;
		} catch (Exception e) {
		}
		return Lists.newArrayList();
	}
	
	private String getDataAsString(String path) {
		try {
			byte[] bb = rpcClientConfig.getZk().getData(path, false, new Stat());
			if (bb != null && bb.length != 0) {
				return new String(bb, "UTF-8");
			}
		} catch (Exception e) {
			log.error("getDataAsString_failed:", e);
		}
		return null;
	}
	
	private String dynamicBuildEndpoint() {
		List<String> children = getChildren(zkBase(), true);
		List<String> groupServers = getChildren(zkGroupBase + "/" + rpcClientConfig.getGroup(), false);
		
		StringBuilder sb = new StringBuilder();
		for (String node : children) {
			try {
				String data = getDataAsString(zkBase() + "/" + node);
				@SuppressWarnings("unchecked")
				Map<String, String> obj = mapper.readValue(data, Map.class);
				if (obj == null || !obj.containsKey("rpc_interfaces")) {
					continue;
				}
				if (!groupServers.contains(node)) {
					//如果该组不包含该ip，则忽略
					continue;
				}
				String v = obj.get("rpc_interfaces");
				int port = 11001;
				if (obj.get("rpc_port") != null) {
					port = NumberUtils.toInt(obj.get("rpc_port")) + 1;
				}
				String[] intfcs = StringUtils.split(v, ";");
				for (String intfc : intfcs) {
					if (StringUtils.endsWithIgnoreCase(intfc, interfaceName)) {
						sb.append(node);
						sb.append(":" + port);
						sb.append(';');
					}
				}
			} catch (Exception e) {
				log.error("dynamicBuildEndpoint_failed:", e);
			}
		}
		return sb.toString();
	}
	
	private void createRpcStub() {
		/*
		 * register myself as a watcher every time 
		 * 'cause watcher is triggered only once by zk
		 */
		try {
			rpcClientConfig.getZk().getChildren(zkBase(), this);
		} catch (Exception e) {
			log.warn("createRpcStub_failed");
		}
		
		String endpoint = rpcClientConfig.getStaticRpcEndpoint();
		if (!rpcClientConfig.isUsingStaticRpcEndpoint()) {
			endpoint = dynamicBuildEndpoint();
			if (endpoint.length() == 0) {
				log.error("doomed!no_alived_service_provider");
				return;
			}
		} else {
			if (cf != null) return;
		}
		
		ClientFactory<Object> cf0 = ClientFactory.createFactory(interfaceCls);
		cf0.setUrl(endpoint);
		cf0.setTimeout(3000);
		cf = cf0;//atomic switch
		log.warn("init_rpcstub_success:" + interfaceCls);
	}
	
	private String zkBase() {
		return "/app/" + rpcClientConfig.getAppName();
	}

	public void setRpcClientConfig(RpcClientConfig rpcClientConfig) {
		this.rpcClientConfig = rpcClientConfig;
	}

	@Override
	public void process(WatchedEvent event) {
		//once get here we recreate the stub
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					createRpcStub();
					timer = null;
				}
			}, 1000 * 10);
		}
	}

}
