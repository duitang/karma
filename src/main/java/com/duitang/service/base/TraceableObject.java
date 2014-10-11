package com.duitang.service.base;

import java.io.Closeable;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.Mixin;

public class TraceableObject<T> {

	public T createTraceableInstance(final Object inst, final Class clz, final String clientid, Closeable closeapi) {
		T ret = (T) Enhancer.create(clz, new TraceProxy(inst, clientid, closeapi));
		if (closeapi == null) {
			return ret;
		}
		return (T) Mixin.create(new Object[] { ret, closeapi });
	}

}

class TraceProxy implements MethodInterceptor {

	protected String clientid;
	protected Object proxy;
	protected Closeable closeapi;

	public TraceProxy(Object proxy, String clientid, Closeable closeapi) {
		this.proxy = proxy;
		this.clientid = clientid;
		this.closeapi = closeapi;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy mproxy) throws Throwable {
		if (this.closeapi != null) {
			if ("close".equals(method.getName())) {
				this.closeapi.close();
				return null;
			}
		}
		long ts = System.nanoTime();
		Object ret = null;
		boolean f = false;
		try {
			ret = mproxy.invoke(proxy, args);
		} catch (Exception e) {
			f = true;
		}
		ts = System.nanoTime() - ts;
		MetricCenter.methodMetric(clientid, method.getName(), ts, f);
		return ret;
	}

}