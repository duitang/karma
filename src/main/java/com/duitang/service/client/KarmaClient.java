package com.duitang.service.client;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.duitang.service.KarmaException;
import com.duitang.service.meta.BinaryPacketData;

public class KarmaClient<T> implements MethodInterceptor {

	protected KarmaIoSession iochannel;
	protected String domainName;
	protected Set<String> cutoffNames;
	protected AtomicLong uuid = new AtomicLong(0);
	protected long timeout = 500;

	static public <T> T createKarmaClient(Class<T> iface, KarmaIoSession iochannel) throws KarmaException {
		if (!iface.isInterface()) {
			throw new KarmaException("not a valid interface: " + iface.getName());
		}

		KarmaClient client = new KarmaClient(iface, iochannel);
		client.setTimeout(iochannel.getTimeout());
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(iface);
		enhancer.setCallback(client);
		return (T) enhancer.create();
	}

	KarmaClient(Class<T> iface, KarmaIoSession io) throws KarmaException {
		this.iochannel = io;
		this.domainName = iface.getName();
		this.cutoffNames = new HashSet<String>();
		for (Method m : iface.getDeclaredMethods()) {
			cutoffNames.add(m.getName());
		}
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
		if (!cutoffNames.contains(name)) {
			return proxy.invokeSuper(obj, args);
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
			throw new KarmaException("call method[" + name + "] timeout / error ", e);
		}
		return ret;
	}
}
