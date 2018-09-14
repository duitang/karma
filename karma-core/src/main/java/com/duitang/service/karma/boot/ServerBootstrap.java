package com.duitang.service.karma.boot;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.handler.ReflectRPCHandler;
import com.duitang.service.karma.router.JavaRouter;
import com.duitang.service.karma.server.CoreEnhanced;
import com.duitang.service.karma.server.ServiceConfig;
import com.duitang.service.karma.server.TCPServer;
import com.duitang.service.karma.support.NameUtil;

public class ServerBootstrap {

	static {
		// just make sure 2 config is loaded
		Class clz = null;
		clz = KarmaServerConfig.class;
		System.err.println("loading ...... " + clz.getName());
		clz = null;
	}

	protected ServiceConfig conf = new ServiceConfig();

	protected ReflectRPCHandler rpc0;
	protected JavaRouter javaRouter;
	protected TCPServer tcp;

	protected StringBuilder info = new StringBuilder();
	protected int maxQueuingLatency = 500;// 请求最大等待时间（即从进入队列到真正被worker执行）

	protected boolean online = false;
	protected Date created = new Date();

	protected void initRPCService(ServiceConfig conf) throws KarmaException {
		rpc0 = new ReflectRPCHandler();
		rpc0.setConf(conf);
		rpc0.init();
	}

	protected void initRouter(RPCHandler rpc0) {
		javaRouter = new JavaRouter();
		javaRouter.setHandler(rpc0);
		javaRouter.setMaxQueuingLatency(maxQueuingLatency);
	}

	public void setMaxQueuingLatency(int maxQueuingLatency) {
		this.maxQueuingLatency = maxQueuingLatency;
	}

	public void addService(Class serviceType, Object service) {
		conf.addService(serviceType, service);
		String clientid = NameUtil.genClientIdFromCode();
		// MetricCenter.initMetric(serviceType, clientid);
		serviceInfo(serviceType, info, clientid, 0);
	}

	/**
	 * <pre>
	 * http = {$port}
	 * netty = {$port+1}
	 * </pre>
	 * @param port listening port
	 * @throws Exception default exception
	 */
	public void startUp(int port) throws Exception {
		initRPCService(conf);
		initRouter(rpc0);

		tcp = new TCPServer();
		tcp.setRouter(javaRouter);
		tcp.setPort(port);
		tcp.start();
		try {
			KarmaServerConfig.clusterAware.registerWrite(tcp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("TCP SERVER LISTENING AT PORT: " + (port));

		System.err.println(info);

		// now enhance extra RPC service
		CoreEnhanced extraService = KarmaFinders.findProtocolSupport();
		if (extraService != null) {
			extraService.enhanced(this);
		}

		online = true;
	}

	/**
	 *
	 * @param serviceType interface types of services
	 * @param service implement with interface type
	 * @param port listening port
	 * @throws Exception errors
	 */
	public void startUp(Class[] serviceType, Object[] service, int port) throws Exception {
		if (serviceType.length != service.length) {
			throw new Exception("not same length of interface and implements");
		}
		for (int i = 0; i < serviceType.length; i++) {
			conf.addService(serviceType[i], service[i]);
		}

		startUp(port);
	}

	public void shutdown() {
		// KarmaClient.shutdownIOPool();
		online = false;
		if (tcp != null) {
			try {
				KarmaServerConfig.clusterAware.unRegisterWrite(tcp);
			} catch (KarmaException e) {
				e.printStackTrace();
			}
			tcp.stop();
		}
		if (javaRouter != null) {
			try {
				javaRouter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			javaRouter = null;
		}
	}

	static public void serviceInfo(Class serviceType, StringBuilder sb, String clientid, int port) {
		if (serviceType == null) {
			return;
		}

		try {
			sb.append(serviceType.getName()).append("  ##############  ").append(clientid).append("\n");
			Method methlist[] = serviceType.getDeclaredMethods();
			for (int i = 0; i < methlist.length; i++) {
				Method m = methlist[i];
				sb.append(" => ").append(m.getName()).append(" { ");
				Class pvec[] = m.getParameterTypes();
				for (int j = 0; j < pvec.length; j++) {
					sb.append(pvec[j]).append(",").append("\n    ");
				}
				sb.replace(sb.length() - 1, sb.length(), " ----> ");
				Class evec[] = m.getExceptionTypes();
				for (int j = 0; j < evec.length; j++) {
					sb.append(evec[j]).append(",").append("\n    ");
				}
				sb.replace(sb.length() - 1, sb.length(), " = ");
				sb.append(m.getReturnType()).append(" } ").append("\n");
			}
		} catch (Throwable e) {
			// ignore
			e.printStackTrace(System.err);
		}

	}

	public JavaRouter getCoreRouter() {
		return javaRouter;
	}

	public ReflectRPCHandler getCoreHandler() {
		return rpc0;
	}

	public boolean isOnline() {
		return online;
	}

	public Date getCreated() {
		return created;
	}

}
