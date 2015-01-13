package com.duitang.service.karma.base;

import java.io.IOException;
import java.lang.reflect.Method;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.JsonRPCHandler;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.handler.ReflectRPCHandler;
import com.duitang.service.karma.router.JavaRouter;
import com.duitang.service.karma.router.JsonRouter;
import com.duitang.service.karma.server.HTTPServer;
import com.duitang.service.karma.server.ServiceConfig;
import com.duitang.service.karma.server.TCPServer;

public class ServerBootstrap {

	protected ServiceConfig conf = new ServiceConfig();

	protected ReflectRPCHandler rpc0;
	protected JsonRPCHandler rpc1;

	protected JavaRouter javaRouter;
	protected JsonRouter jsonRouter;

	protected HTTPServer http;
	protected TCPServer tcp;

	protected StringBuilder info = new StringBuilder();

	protected void initRPCService(ServiceConfig conf) throws KarmaException {
		rpc0 = new ReflectRPCHandler();
		rpc0.setConf(conf);
		rpc0.init();
		rpc1 = new JsonRPCHandler(rpc0);
	}

	protected void initRouter(RPCHandler rpc0, RPCHandler rpc1) {
		javaRouter = new JavaRouter();
		javaRouter.setHandler(rpc0);

		jsonRouter = new JsonRouter();
		jsonRouter.setHandler(rpc1);
	}

	public void addService(Class serviceType, Object service) {
		conf.addService(serviceType, service);
		String clientid = MetricCenter.genClientIdFromCode();
//		MetricCenter.initMetric(serviceType, clientid);
		serviceInfo(serviceType, info, clientid, 0);
	}

	/**
	 * <pre>
	 * http = {$port} 
	 * netty = {$port+1}
	 * </pre>
	 * 
	 * @param port
	 * @throws IOException
	 */
	public void startUp(int port) throws Exception {
		initRPCService(conf);
		initRouter(rpc0, rpc1);

		http = new HTTPServer();
		http.setRouter(jsonRouter);
		http.setPort(port);
		http.start();
		System.err.println("HTTP SERVER LISTENING AT PORT: " + port);

		tcp = new TCPServer();
		tcp.setRouter(javaRouter);
		tcp.setPort(port + 1);
		tcp.start();
		System.err.println("TCP SERVER LISTENING AT PORT: " + (port + 1));

		System.err.println(info);
	}

	/**
	 * @deprecated
	 * @param serviceType
	 * @param service
	 * @param port
	 * @throws Exception
	 */
	public void startUp(Class serviceType, Object service, int port) throws IOException {
		try {
			startUp(new Class[] { serviceType }, new Object[] { service }, port);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * @deprecated
	 * @param port
	 * @param protocol
	 * @throws IOException
	 */
	public void startUp(int port, String protocol) throws IOException {
		try {
			startUp(port);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * 
	 * @param serviceType
	 * @param service
	 * @param port
	 * @throws Exception
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

	/**
	 * @deprecated
	 * @param serviceType
	 * @param service
	 * @param port
	 * @param protocol
	 * @throws Exception
	 */
	public void startUp(Class[] serviceType, Object[] service, int port, String protocol) throws Exception {
		startUp(serviceType, service, port);
	}

	public void shutdown() {
		if (http != null) {
			http.stop();
		}
		if (tcp != null) {
			tcp.stop();
		}
	}

	protected String genServiceName(Object... services) {
		String ret = "";
		if (services != null) {
			StringBuilder sb = new StringBuilder();
			String[] name = null;
			for (Object svc : services) {
				name = svc.toString().split("\\.");
				sb.append(name[name.length - 1]).append(",");
			}
			if (sb.length() > 0) {
				sb.delete(sb.length() - 1, sb.length());
			}
			ret = sb.toString();
		}
		return ret;
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

}
