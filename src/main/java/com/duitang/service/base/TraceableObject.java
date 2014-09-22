package com.duitang.service.base;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class TraceableObject<T> {

	protected HashMap<String, Enhancer> cached = new HashMap<String, Enhancer>();

	public T createTraceableInstance(final Object inst, final Class clz, final String clientid) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clz);
		enhancer.setCallback(new TraceProxy(inst, clientid));
		return (T) enhancer.create();
	}

}

class TraceProxy implements MethodInterceptor {

	protected String clientid;
	protected Object proxy;

	public TraceProxy(Object proxy, String clientid) {
		this.proxy = proxy;
		this.clientid = clientid;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy mproxy) throws Throwable {
		long ts = System.currentTimeMillis();
		Object ret = null;
		boolean f = false;
		try {
			ret = mproxy.invoke(proxy, args);
		} catch (Exception e) {
			f = true;
		}
		MetricCenter.methodMetric(clientid + ":" + method.getName(), ts, f);
		return ret;
	}

}