package com.duitang.service.base;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.reflect.ReflectData;

public class MetricalReflectRequestor extends ReflectRequestor implements Closeable, Validation {

	final static protected HashMap<String, Method> escapeMethodName = new HashMap<String, Method>();

	protected String clientid;
	protected Transceiver closeableTrans;
	protected boolean valid;

	static void addLocalMethods(Method[] ms) {
		for (Method m : ms) {
			Method mm;
			String nm = "";
			try {
				mm = MetricalReflectRequestor.class.getMethod(m.getName(), m.getParameterTypes());
				escapeMethodName.put(m.getName(), mm);
				nm = "add to local";
			} catch (Exception e) {
				escapeMethodName.put(m.getName(), null);
				nm = "skip local";
			}
			System.err.println(nm + " method .... " + m.getName() + " @MetricalReflectRequestor");
		}
	}

	static {
		// object standard methods
		addLocalMethods(Object.class.getDeclaredMethods());

		// then force self Interface
		for (Class face : MetricalReflectRequestor.class.getInterfaces()) {
			addLocalMethods(face.getDeclaredMethods());
		}
	}

	protected void initClientName() {
		if (clientid == null) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			for (int i = 0; i < stacktrace.length; i++) {
				StackTraceElement e = stacktrace[i];
				if (e.getMethodName() != null && !e.getClassName().startsWith("com.duitang.service.base")) {
					clientid = MetricCenter.getHostname() + "|" + e.getFileName() + "@" + e.getLineNumber() + ":" + e.getMethodName();
				}
			}
		}
		if (clientid == null) {
			clientid = "";
		}
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public Transceiver getCloseableTrans() {
		return closeableTrans;
	}

	public void setCloseableTrans(Transceiver closeableTrans) {
		this.closeableTrans = closeableTrans;
	}

	public MetricalReflectRequestor(Class<?> iface, Transceiver transceiver, ReflectData data) throws IOException {
		super(iface, transceiver, data);
		this.closeableTrans = transceiver;
	}

	public MetricalReflectRequestor(Class<?> iface, Transceiver transceiver) throws IOException {
		super(iface, transceiver);
		this.closeableTrans = transceiver;
	}

	public MetricalReflectRequestor(Protocol protocol, Transceiver transceiver, ReflectData data) throws IOException {
		super(protocol, transceiver, data);
		this.closeableTrans = transceiver;
	}

	public MetricalReflectRequestor(Protocol protocol, Transceiver transceiver) throws IOException {
		super(protocol, transceiver);
		this.closeableTrans = transceiver;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (escapeMethodName.containsKey(method.getName())) {
			Method mm = escapeMethodName.get(method.getName());
			if (mm == null) {
				// System.out.println("skip native ............... " +
				// method.getName());
				return null;
			}
			// System.out.println("invoked native ............... " +
			// method.getName());
			return method.invoke(this, args);
		}
		// System.out.println("invoked ............... " + method.getName());

		Object ret = null;
		boolean fail = false;
		long ts = System.nanoTime();
		Throwable rett = null;
		try {
			ret = super.invoke(proxy, method, args);
			valid = true;
		} catch (Throwable t) {
			fail = true;
			valid = false;
			rett = t;
		} finally {
			ts = System.nanoTime() - ts;
			MetricCenter.methodMetric(clientid, method.getName(), ts, fail);
		}
		if (fail) {
			throw rett;
		}
		return ret;
	}

	@Override
	public void close() throws IOException {
		closeableTrans.close();
	}

	@Override
	public boolean isValid() {
		boolean b = false;
		if (closeableTrans instanceof Validation) {
			b = ((Validation) closeableTrans).isValid();
		} else {
			b = closeableTrans.isConnected();
		}
		return valid && b;
	}

	public static <T> T getClient(Class<T> iface, Transceiver transciever) throws IOException {
		return getClient(iface, transciever, new ReflectData(iface.getClassLoader()));
	}

	@SuppressWarnings("unchecked")
	public static <T> T getClient(Class<T> iface, Transceiver transciever, ReflectData reflectData) throws IOException {
		Protocol protocol = reflectData.getProtocol(iface);
		MetricalReflectRequestor req = new MetricalReflectRequestor(protocol, transciever, reflectData);
		req.initClientName();
		req.getRemote();
		return (T) Proxy.newProxyInstance(reflectData.getClassLoader(), new Class[] { iface, Closeable.class, Validation.class }, req);
	}
}
