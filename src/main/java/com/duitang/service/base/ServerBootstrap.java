package com.duitang.service.base;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.Opcodes;
import net.sf.cglib.proxy.Mixin;

import org.apache.avro.ipc.HttpServer;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.apache.avro.ipc.specific.SpecificResponder;

public class ServerBootstrap {

	protected Server server;
	protected String clientid;
	protected String hostname;

	protected Object[] proxiedService;
	protected Class gatewayInterface;
	protected Object gatewayService;

	protected List<Class> origin_stypes = new ArrayList<Class>();
	protected List<Object> origin_service = new ArrayList<Object>();

	protected String SERVICE_INTERFACE = ServerBootstrap.class.getPackage().getName() + ".FacadeServiceImpl";

	protected static Class genInterface(final String destName, Class[] clz) {
		final String[] interfaces = new String[clz.length];
		int i = 0;
		for (Class<?> interfac : clz) {
			interfaces[i] = interfac.getName().replace('.', '/');
			i++;
		}
		Class<?> klass = new ClassLoader(ServerBootstrap.class.getClassLoader()) {
			public Class<?> defineClass() {
				ClassWriter cw = new ClassWriter(0);
				cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE,
				        destName.replaceAll("\\.", "/"), null, "java/lang/Object", interfaces);
				cw.visitEnd();
				byte[] bytes = cw.toByteArray();
				return defineClass(destName.replaceAll("/", "\\."), bytes, 0, bytes.length);
			}
		}.defineClass();
		return klass;
	}

	public void addService(Class serviceType, Object service) {
		origin_stypes.add(serviceType);
		origin_service.add(service);
	}

	public Class getGatewayInterface() {
		return gatewayInterface;
	}

	public void startUp(int port, String protocol) throws IOException {
		Class[] t = origin_stypes.toArray(new Class[origin_stypes.size()]);
		Object[] s = origin_service.toArray(new Object[origin_service.size()]);
		startUp(t, s, port, protocol);
	}

	@Deprecated
	public void startUp(Class serviceType, Object service, int port) throws IOException {
		startUp(serviceType, service, port, "http");
	}

	@Deprecated
	public void startUp(Class serviceType, Object service, int port, String protocol) throws IOException {
		clientid = MetricCenter.getHostname() + "|" + service.toString();
		// traceAllService(new Class[] { serviceType }, new Object[] { service
		// }, new Closeable[1]);
		MetricCenter.initMetric(serviceType, clientid);
		gatewayInterface = serviceType;
		gatewayService = service;
		if (protocol.equalsIgnoreCase("http")) {
			server = new HttpServer(new SpecificResponder(gatewayInterface, gatewayService), port);
		} else {
			server = new NettyServer(new SpecificResponder(gatewayInterface, gatewayService), new InetSocketAddress(
			        port));
		}
		server.start();
	}

	public void startUp(Class[] serviceType, Object[] service, int port, String protocol) throws IOException {
		clientid = MetricCenter.getHostname() + "|" + genServiceName(service);
		// traceAllService(serviceType, service, new Closeable[service.length]);
		gatewayInterface = mixAllService(serviceType);
		gatewayService = mixAllImpls(serviceType, service);
		MetricCenter.initMetric(gatewayInterface, clientid);
		if (protocol.equalsIgnoreCase("http")) {
			server = new HttpServer(new ReflectResponder(gatewayInterface, gatewayService), port);
		} else {
			server = new NettyServer(new ReflectResponder(gatewayInterface, gatewayService),
			        new InetSocketAddress(port));
		}
		server.start();
	}

	protected void traceAllService(Class[] sTypes, Object[] impls, Closeable[] closeapi) {
		proxiedService = new Object[impls.length];
		for (int i = 0; i < impls.length; i++) {
			proxiedService[i] = TraceableObject.create(impls[i], sTypes[i], clientid, closeapi[i]);
		}
	}

	protected Class mixAllService(Class[] serviceType) {
		return genInterface(SERVICE_INTERFACE, serviceType);
	}

	protected Object mixAllImpls(Class[] serviceType, Object[] impls) {
		return Mixin.create(serviceType, impls);
	}

	protected String genServiceName(Object[] services) {
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

	public void shutdown() {
		if (server != null) {
			server.close();
		}
	}

	public String getClientid() {
		return clientid;
	}

	public void serviceInfo(Class serviceType, StringBuilder sb, String protocol, int port) {
		if (serviceType == null) {
			return;
		}

		try {
			sb.append(serviceType.getName()).append("  ##############  ").append(protocol).append("@").append(port)
			        .append("\n");
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
		}

	}

}
