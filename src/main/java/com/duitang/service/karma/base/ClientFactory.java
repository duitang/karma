package com.duitang.service.karma.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import com.duitang.service.karma.client.KarmaClient;
import com.duitang.service.karma.client.KarmaIoSession;

public abstract class ClientFactory<T> implements ServiceFactory<T> {

	protected Logger err = Logger.getLogger("error");

	protected String url;
	protected List<String> serviceURL;
	protected AtomicInteger hashid = new AtomicInteger(0);
	protected int sz;
	protected int timeout = 500;
	protected String clientid;
	protected GenericObjectPool<KarmaClient<T>> cliPool = forceCreatePool();

	public ClientFactory() {
		this(null);
	}

	public ClientFactory(String clientid) {
		this.clientid = clientid;
		initClientName();
	}

	private void initClientName() {
		if (clientid == null) {
			clientid = MetricCenter.genClientIdFromCode();
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		String[] urlitems = url.split(";");
		this.serviceURL = new ArrayList<String>();
		for (String u : urlitems) {
			this.serviceURL.add(u);
		}
		this.sz = this.serviceURL.size();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public T create() { // for python performance issue no exception
		if (sz == 0) {
			new RuntimeException("no remote url find? please setUrl(String url)").printStackTrace();
			return null;
		}
		try {
			KarmaClient<T> client = cliPool.borrowObject(timeout);
			return client.getService();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void release(T client) {
		if (client == null) {
			return;
		}
		if (!(client instanceof LifeCycle) && !(client instanceof KarmaClientInfo)) {
			return;
		}
		boolean v = true;
		v = ((LifeCycle) client).isAlive();

		if (v) {
			cliPool.returnObject(((KarmaClientInfo) client).getProxy());
		} else {
			try {
				cliPool.invalidateObject(((KarmaClientInfo) client).getProxy());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static <T1> ClientFactory<T1> createFactory(final Class<T1> clz) {
		final String name = clz.getName();
		ClientFactory<T1> ret = new ClientFactory<T1>() {

			@Override
			public String getServiceName() {
				return name;
			}

			@Override
			public Class getServiceType() {
				return clz;
			}

		};
		return ret;
	}

	protected GenericObjectPool<KarmaClient<T>> forceCreatePool() {
		GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
		cfg.setMaxIdle(30);
		cfg.setMinIdle(5);
		cfg.setMaxTotal(150);
		cfg.setTestWhileIdle(false);
		cfg.setBlockWhenExhausted(true);
		cfg.setMaxWaitMillis(timeout);
		cfg.setMinEvictableIdleTimeMillis(120000);
		// cfg.setTestOnReturn(true); // may release it if idle
		cfg.setTestOnBorrow(true); // may release it if idle
		return new GenericObjectPool(new ReflectServiceFactory(), cfg);
	}

	class ReflectServiceFactory implements PooledObjectFactory<KarmaClient<T>> {

		@Override
		public PooledObject<KarmaClient<T>> makeObject() throws Exception {
			try {
				Integer iid = Math.abs(hashid.incrementAndGet()) % sz;
				String u = serviceURL.get(iid);
				KarmaIoSession session = new KarmaIoSession(u, timeout);
				KarmaClient<T> ret = KarmaClient.createKarmaClient(getServiceType(), session, clientid);
				// ret.init();
				return new DefaultPooledObject<KarmaClient<T>>(ret);
			} catch (Exception e) {
				err.error("create for service: " + url, e);
				throw e;
			}
		}

		@Override
		public void destroyObject(PooledObject<KarmaClient<T>> p) throws Exception {
			KarmaClient<T> obj = p.getObject();
			// System.out.println("destroy ..... " + obj);
			obj.close();
		}

		@Override
		public boolean validateObject(PooledObject<KarmaClient<T>> p) {
			KarmaClient<T> obj = p.getObject();
			// System.out.println("checking ..... " + ((Validation)
			// obj).isValid() + " ---> " + obj);
			return obj.isAlive();
		}

		@Override
		public void activateObject(PooledObject<KarmaClient<T>> p) throws Exception {
			// ignore
			// System.out.println("...............active " + p.getObject());
			KarmaClient<T> obj = p.getObject();
			obj.init();
		}

		@Override
		public void passivateObject(PooledObject<KarmaClient<T>> p) throws Exception {
			// ignore
		}

	}

}
