package com.duitang.service.base;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.avro.ipc.HttpServer;
import org.apache.avro.ipc.specific.SpecificResponder;

public class ServerBootstrap {

	protected HttpServer server;
	protected String clientid;
	protected TraceableObject tracer;
	protected Object proxiedService;
	protected String hostname;

	public void startUp(Class serviceType, Object service, int port) throws IOException {
		clientid = MetricCenter.getHostname() + "|" + service.toString();
		MetricCenter.initMetric(serviceType, clientid);
		tracer = new TraceableObject();
		proxiedService = tracer.createTraceableInstance(service, serviceType, clientid);
		server = new HttpServer(new SpecificResponder(serviceType, proxiedService), port);
		server.start();
	}

	public void shutdown() {
		if (server != null) {
			server.close();
		}
	}

	public String getClientid() {
		return clientid;
	}

	public void serviceInfo(Class serviceType, StringBuilder sb) {
		if (serviceType == null) {
			return;
		}

		try {
			sb.append(serviceType.getName()).append("\n");
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
