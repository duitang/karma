package com.duitang.service.client;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.duitang.service.KarmaException;
import com.duitang.service.KarmaRuntimeException;
import com.duitang.service.base.KarmaClientInfo;
import com.duitang.service.base.LifeCycle;
import com.duitang.service.meta.BinaryPacketData;

public class KarmaClient<T> implements MethodInterceptor, LifeCycle, KarmaClientInfo {

	final static public String CLINET_ATTR_NAME = "_KARMACLIENT_";
	final static protected Map<String, Method> mgrCallbacks;

	protected KarmaIoSession iochannel;
	protected String domainName;
	protected Map<String, Boolean> cutoffNames;
	protected AtomicLong uuid = new AtomicLong(0);
	protected long timeout = 500;
	protected T dummy;

	static {
		mgrCallbacks = new HashMap<String, Method>();
		Class[] ifaces = new Class[] { LifeCycle.class, KarmaClientInfo.class };
		for (Class clz : ifaces) {
			for (Method m : clz.getDeclaredMethods()) {
				mgrCallbacks.put(m.getName(), m);
			}
		}
	}

	static public <T> KarmaClient<T> createKarmaClient(Class<T> iface, KarmaIoSession iochannel) throws KarmaException {
		if (!iface.isInterface()) {
			throw new KarmaException("not a valid interface: " + iface.getName());
		}
		KarmaClient client = new KarmaClient(iface, iochannel);
		client.setTimeout(iochannel.getTimeout());
		client.dummy = (T) Enhancer.create(null, new Class[] { iface, LifeCycle.class, KarmaClientInfo.class }, client);
		return client;
	}

	KarmaClient(Class<T> iface, KarmaIoSession io) throws KarmaException {
		this.iochannel = io;
		this.domainName = iface.getName();
		this.cutoffNames = new HashMap<String, Boolean>();
		Boolean useEx = false;
		for (Method m : iface.getDeclaredMethods()) {
			for (Class eClz : m.getExceptionTypes()) {
				if (KarmaException.class.isAssignableFrom(eClz)) {
					useEx = true;
				}
			}
			cutoffNames.put(m.getName(), useEx);
		}
	}

	public T getService() {
		return dummy;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		String name = method.getName();
		if (!cutoffNames.containsKey(name) && !mgrCallbacks.containsKey(name)) {
			return proxy.invokeSuper(obj, args);
		}
		if (mgrCallbacks.containsKey(name)) {
			Method m = mgrCallbacks.get(name);
			return m.invoke(this, args);
		}
		BinaryPacketData data = new BinaryPacketData();
		data.domain = domainName;
		data.method = name;
		data.param = args;
		data.uuid = uuid.incrementAndGet();
		KarmaRemoteLatch latch = new KarmaRemoteLatch(timeout);
		iochannel.setAttribute(latch);
		iochannel.write(data);
		Object ret = null;
		try {
			ret = latch.getResult();
		} catch (Throwable e) {
			throw new KarmaRuntimeException("call method[" + name + "] timeout / error @" + iochannel.reportError(), e);
		}
		return ret;
	}

	@Override
	public void close() throws IOException {
		iochannel.close();
	}

	@Override
	public void init() throws Exception {
		iochannel.init();
		iochannel.session.setAttributeIfAbsent(CLINET_ATTR_NAME, this);
	}

	@Override
	public boolean isAlive() {
		return iochannel.isAlive();
	}

	@Override
	public KarmaClient getProxy() {
		return this;
	}

}
