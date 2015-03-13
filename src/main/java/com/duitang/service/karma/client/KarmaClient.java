package com.duitang.service.karma.client;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
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
import com.duitang.service.karma.meta.RPCConfig;
import com.duitang.service.karma.support.CCT;
import com.duitang.service.karma.support.TraceChainDO;

@SuppressWarnings("rawtypes")
public class KarmaClient<T> implements MethodInterceptor, KarmaClientInfo {

	final static public String CLINET_ATTR_NAME = "_KARMACLIENT_";
	final static protected Map<String, Method> mgrCallbacks;
	final static protected KarmaIOPool pool = new KarmaIOPool();
	final static protected Logger error = LoggerFactory.getLogger(KarmaClient.class);
	final static protected AtomicBoolean lock = new AtomicBoolean(false);
	
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
		Class[] ifaces = new Class[]
			{
			        LifeCycle.class,
			        KarmaClientInfo.class };
		for (Class clz : ifaces) {
			for (Method m : clz.getDeclaredMethods()) {
				mgrCallbacks.put(m.getName(), m);
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				shutdownIOPool();
			}

		});
	}

	public static void reset(String group, List<String> urls) {
	    if (lock.compareAndSet(false, true)) {
	        System.out.println("obtained:" + group);
	        pool.resetPool();
            WRRBalancer.getInstance(group, urls).reload(urls);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    lock.compareAndSet(true, false);
                }
            }, 6000);
	    }
	}
	
	public static void shutdownIOPool() {
		pool.close();
	}

	static public <T> KarmaClient<T> createKarmaClient(
	    Class<T> iface, 
	    List<String> urls, 
	    String clientid, 
	    String group
	) throws KarmaException {
		return createKarmaClient(iface, urls, clientid, group, 500);
	}

	@SuppressWarnings("unchecked")
    static public <T> KarmaClient<T> createKarmaClient(
        Class<T> iface, 
        List<String> urls, 
        String clientid, 
        String group, 
        long timeout
    ) throws KarmaException {
		if (!iface.isInterface()) {
			throw new KarmaException("not a valid interface: " + iface.getName());
		}
		// caution: fair load is a hint for there is a flushed version from ZK
		//ClusterZKRouter rt = ClusterZKRouter.createRouter(group, ClusterZKRouter.fairLoad(urls));
		IOBalance iob = WRRBalancer.getInstance(group, urls);
		KarmaClient client = new KarmaClient(iface, iob);
		client.timeout = timeout;
		client.clientid = clientid;
		client.dummy = (T) Enhancer.create(
		    null, 
		    new Class[] {iface, KarmaClientInfo.class}, 
		    client
		);
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
		RPCConfig rpcConfig = new RPCConfig();
		TraceChainDO tc = CCT.get();
		if (tc != null) {
		    tc = tc.clone();
		    rpcConfig.addConf(CCT.RPC_CONF_KEY, tc);
		}
		rpcConfig.addConf("timebase", System.currentTimeMillis());
		
		BinaryPacketData data = new BinaryPacketData();
		data.domain = domainName;
		data.method = name;
		data.param = args;
		data.uuid = uuid.incrementAndGet();
		data.conf = rpcConfig;
		
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
			CCT.mergeTraceChain(latch.getRemoteTc());
		} catch (Throwable e) {
		    router.fail(u);
			flag = true;
			boolean reachable = true;
			if (iosession != null) {
				pong = iosession.ping(uuid.incrementAndGet());
				error.debug("ping " + u + " ok = " + pong);
				if (!pong) reachable = iosession.reachable();
			}
			String err = String.format("%s call method[%s]@%s timeout/err_pong=%s,reachable=%s", iosession, name, u, pong, reachable);
			throw new KarmaRuntimeException(err, e);
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
