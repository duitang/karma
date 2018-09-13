package com.duitang.service.karma.client;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaOverloadException;
import com.duitang.service.karma.KarmaRuntimeException;
import com.duitang.service.karma.KarmaTimeoutException;
import com.duitang.service.karma.boot.KarmaClientConfig;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.RPCConfig;
import com.duitang.service.karma.support.NameUtil;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TraceContextHolder;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

@SuppressWarnings("rawtypes")
public class KarmaClient<T> implements MethodInterceptor {

	static {
		// just make sure 2 config is loaded
		Class clz = null;
		clz = KarmaClientConfig.class;
		System.err.println("loading ...... " + clz.getName());
	}

	static KarmaIOPool pool = new KarmaIOPool();
	final static protected Logger error = LoggerFactory.getLogger(KarmaClient.class);
	final static private Long DEFAULT_TIMEOUT = 1000L;

	protected String domainName;
	protected Map<String, Boolean> cutoffNames;
	protected long timeout = DEFAULT_TIMEOUT;
	protected T dummy;
	protected IOBalance router;
	protected String group;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdownIOPool();
				KarmaIoSession.shutdown();
			}
		});
	}

	synchronized public static void bindGroup(String group, List<String> urls) throws KarmaException {
		if (pool != null) {
			KarmaClientConfig.bindBalance(group, urls);
			pool.resetPool(urls);
		}
	}

	synchronized public static void shutdownIOPool() {
		if (pool != null) {
			KarmaIOPool p = pool;
			pool = new KarmaIOPool();
			p.close();
			KarmaIoSession.shutdown();
		}
	}

	static public <T> KarmaClient<T> createKarmaClient(Class<T> iface, String group) throws KarmaException {
		return createKarmaClient(iface, null, group, DEFAULT_TIMEOUT);
	}

	static public <T> KarmaClient<T> createKarmaClient(Class<T> iface, List<String> urls) throws KarmaException {
		return createKarmaClient(iface, urls, null, DEFAULT_TIMEOUT);
	}

	static public <T> KarmaClient<T> createKarmaClient(Class<T> iface, List<String> urls, String group, long timeout)
			throws KarmaException {
		if (!iface.isInterface()) {
			throw new KarmaException("not a valid interface: " + iface.getName());
		}
		IOBalance iob = KarmaClientConfig.getOrCreateIOBalance(group, urls);
		KarmaClient client = new KarmaClient(iface, iob);
		client.group = group;
		client.timeout = timeout;
		client.dummy = Enhancer.create(null, new Class[] { iface }, client);
		return client;
	}

	KarmaClient(Class<T> iface, IOBalance bl) throws KarmaException {
		this.router = bl;
		this.domainName = iface.getName();
		this.cutoffNames = new HashMap<>();
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
		this.timeout = timeout + 1; // a bit more than
	}
	
	public Object invoke(Object obj, Method method, Object[] args) {
		if (!cutoffNames.containsKey(method.getName())) {
			throw new NoSuchMethodError(method.getName());
		}
		obj = obj == null ? dummy : obj;
		try {
			return intercept(obj, method, args, null);
		} catch (Throwable e) {
			throw new KarmaRuntimeException(e);
		}
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		String name = method.getName();
		if (!cutoffNames.containsKey(name)) {
			return proxy.invokeSuper(obj, args);
		}
		TraceCell tc = TraceContextHolder.accquire(true);
		tc.host = NameUtil.getHostname();
		if (tc.parentId == null) {
			tc.sampled = TraceContextHolder.getSampler().sample(obj.getClass().getName(), name, args);
		}
		RPCConfig rpcConfig = new RPCConfig();
		rpcConfig.addConf(TraceCell.TRACE_ID, tc.traceId);
		rpcConfig.addConf(TraceCell.SPAN_ID, tc.spanId);
		rpcConfig.addConf(TraceCell.SAMPLED, tc.sampled);
		tc.clazzName = domainName;
		tc.name = name;
		tc.group = group;

		BinaryPacketData data = new BinaryPacketData();
		data.domain = domainName;
		data.method = name;
		data.param = args;
		data.conf = rpcConfig;

		KarmaRemoteLatch latch = new KarmaRemoteLatch(timeout);
		latch.setTraceCell(tc);
		Object ret = null;
		KarmaIoSession iosession = null;
		String u = null;
		try {
			u = router.next(null);
			iosession = pool.getIOSession(u);
			data.uuid = iosession.getUuid().incrementAndGet();
			latch.setUuid(data.uuid);
			iosession.setTimeout(timeout);
			iosession.setAttribute(latch);
			iosession.write(data);
			tc.active();
			ret = latch.getResult();
		} catch (KarmaOverloadException e) {
			latch.ex = e;
			throw e;
		} catch (Throwable e) {
			latch.ex = e;
			if (e instanceof KarmaTimeoutException) {
				boolean reachable = true;
				if (iosession != null) {
					reachable = iosession.reachable();
				}
				// still possible to get the result, which should be treated as
				// a success
				Object result = latch.readResult();
				if (result != null) {
					ret = result;
					error.info("Hooray! recover the result: " + result);
				} else {
					error.error(iosession + " method: " + name + " timeout, network reachable: " + reachable);
					throw e;
				}
			} else if (e instanceof KarmaException) {
				throw new KarmaRuntimeException(e);
			} else {
				throw e;
			}
		} finally {
			latch.done();
			if (iosession != null) {
				pool.releaseIOSession(iosession);
			}
			router.traceFeed(u, tc);
			TraceContextHolder.release();
			KarmaClientConfig.getTraceVisitor(group).visit(tc);
		}
		return ret;
	}

	public void resetTrace() {
		TraceContextHolder.reset();
	}

}
