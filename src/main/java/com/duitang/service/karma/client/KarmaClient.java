package com.duitang.service.karma.client;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaRuntimeException;
import com.duitang.service.karma.base.KarmaClientInfo;
import com.duitang.service.karma.base.LifeCycle;
import com.duitang.service.karma.base.MetricCenter;
import com.duitang.service.karma.meta.BinaryPacketData;

public class KarmaClient<T> implements MethodInterceptor, KarmaClientInfo {

	final static public String CLINET_ATTR_NAME = "_KARMACLIENT_";
	final static protected Map<String, Method> mgrCallbacks;
	final static protected KarmaIOPool pool = new KarmaIOPool();
	final static protected Logger error = LoggerFactory.getLogger(KarmaClient.class);

	static String zkURL = null;

	protected String clientid;
	protected String domainName;
	protected Map<String, Boolean> cutoffNames;
	protected AtomicLong uuid = new AtomicLong(0);
	protected long timeout = 500;
	protected T dummy;
	protected IOBalance router;

	static {
		mgrCallbacks = new HashMap<String, Method>();
		Class[] ifaces = new Class[] { LifeCycle.class, KarmaClientInfo.class };
		for (Class clz : ifaces) {
			for (Method m : clz.getDeclaredMethods()) {
				mgrCallbacks.put(m.getName(), m);
			}
		}
	}

	static public <T> KarmaClient<T> createKarmaClient(Class<T> iface, List<String> urls, String clientid, String group) throws KarmaException {
		if (!iface.isInterface()) {
			throw new KarmaException("not a valid interface: " + iface.getName());
		}
		// caution: fair load is a hint for there is a flushed version from ZK
		ClusterZKRouter rt = ClusterZKRouter.createRouter(group, ClusterZKRouter.fairLoad(urls));
		KarmaClient client = new KarmaClient(iface, rt);
		client.clientid = clientid;
		client.dummy = (T) Enhancer.create(null, new Class[] { iface, KarmaClientInfo.class }, client);
		return client;
	}

	KarmaClient(Class<T> iface, IOBalance bl) throws KarmaException {
		this.router = bl;
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
		long ts = System.nanoTime();
		BinaryPacketData data = new BinaryPacketData();
		data.domain = domainName;
		data.method = name;
		data.param = args;
		data.uuid = uuid.incrementAndGet();
		KarmaRemoteLatch latch = new KarmaRemoteLatch(timeout);
		latch.setUuid(data.uuid);
		Object ret = null;
		boolean flag = false;
		KarmaIoSession iosession = null;
		String u = null;
		boolean pong = false;
		try {
			u = this.router.next(null);
			iosession = pool.getIOSession(u);
			iosession.setTimeout(timeout);
			iosession.setAttribute(latch);
			iosession.write(data);
			ret = latch.getResult();
			flag = false;
		} catch (Throwable e) {
			flag = true;
			if (iosession != null) {
				pong = iosession.ping();
				error.debug("ping " + u + " ok = " + pong);
			}
			throw new KarmaRuntimeException(iosession + " call method[" + name + "]@" + u + " timeout / error pong = " + pong, e);
		} finally {
			if (iosession != null) {
				pool.releaseIOSession(iosession);
			}
			ts = System.nanoTime() - ts;
			MetricCenter.methodMetric(this.clientid, name, ts, flag);
		}
		return ret;
	}

	@Override
	public KarmaClient getProxy() {
		return this;
	}

}
