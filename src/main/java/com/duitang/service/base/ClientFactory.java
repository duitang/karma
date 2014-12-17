package com.duitang.service.base;

import java.io.Closeable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.ipc.Transceiver;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import com.duitang.service.mina.MinaTransceiver;

public abstract class ClientFactory<T> implements ServiceFactory<T> {

	protected Logger err = Logger.getLogger("error");

	protected String url;
	protected List<URL> serviceURL;
	protected List<Boolean> serviceHTTPProtocol;
	protected AtomicInteger hashid = new AtomicInteger(0);
	protected int sz;
	protected int timeout = 500;
	protected String clientid;
	protected GenericObjectPool<T> cliPool = forceCreatePool();

	public ClientFactory() {
		this(null);
	}

	public ClientFactory(String clientid) {
		this.clientid = clientid;
		initClientName();
		init();
	}

	protected void init() {
		MetricCenter.initMetric(getServiceType(), clientid);
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		String[] urlitems = url.split(";");
		this.serviceURL = new ArrayList<URL>();
		this.serviceHTTPProtocol = new ArrayList<Boolean>();
		boolean isHttp = false;
		URL ur = null;
		for (String u : urlitems) {
			try {
				if (u.startsWith("http")) {
					isHttp = true;
					ur = new URL(u);
				} else {
					isHttp = false;
					if (u.contains("//")) {
						u = u.replaceFirst(".*//", "");
					}
					ur = new URL("http://" + u);
				}
				this.serviceURL.add(ur);
				this.serviceHTTPProtocol.add(isHttp);
			} catch (Exception e) {
				throw new RuntimeException("set url: " + u, e);
			}
		}
		this.sz = this.serviceURL.size();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
		MetricableHttpTransceiver.setTimeout(timeout);
	}

	@Override
	public T create() {
		if (sz == 0) {
			throw new RuntimeException("no remote url find? please setUrl(String url)");
		}
		try {
			return cliPool.borrowObject(timeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void release(T client) {
		if (client == null) {
			return;
		}
		boolean v = true;
		if (client instanceof Validation) {
			v = ((Validation) client).isValid();
		}
		if (v) {
			cliPool.returnObject(client);
		} else {
			try {
				cliPool.invalidateObject(client);
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

	protected GenericObjectPool<T> forceCreatePool() {
		GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
		cfg.setMaxIdle(10);
		cfg.setMinIdle(3);
		cfg.setMaxTotal(200);
		cfg.setTestWhileIdle(false);
		cfg.setBlockWhenExhausted(true);
//		cfg.setTestOnReturn(true); // may release it if error
		GenericObjectPool<T> ret = new GenericObjectPool<T>(new ReflectServiceFactory<T>(), cfg);
		return ret;
	}

	class ReflectServiceFactory<T1 extends T> implements PooledObjectFactory<T> {

		@SuppressWarnings("resource")
		@Override
		public PooledObject<T> makeObject() throws Exception {
			T ret = null;
			try {
				Integer iid = Math.abs(hashid.incrementAndGet()) % sz;
				Transceiver trans = null;
				URL u = serviceURL.get(iid);
				if (serviceHTTPProtocol.get(iid)) {
					trans = new MetricableHttpTransceiver(clientid, u);
					MetricableHttpTransceiver.setTimeout(timeout);
				} else {
					trans = new MinaTransceiver(u.getHost() + ":" + u.getPort(), timeout).init();
				}
				ret = (T) MetricalReflectRequestor.getClient(getServiceType(), trans);
			} catch (Exception e) {
				err.error("create for service: " + url, e);
				throw e;
			}
			return new DefaultPooledObject<T>(ret);
		}

		@Override
		public void destroyObject(PooledObject<T> p) throws Exception {
			T obj = p.getObject();
			// System.out.println("destroy ..... " + obj);
			if (obj instanceof Closeable) {
				((Closeable) obj).close();
			}
		}

		@Override
		public boolean validateObject(PooledObject<T> p) {
			T obj = p.getObject();
			// System.out.println("checking ..... " + ((Validation)
			// obj).isValid() + " ---> " + obj);
			if (obj instanceof Validation) {
				return ((Validation) obj).isValid();
			}
			return true;
		}

		@Override
		public void activateObject(PooledObject<T> p) throws Exception {
		}

		@Override
		public void passivateObject(PooledObject<T> p) throws Exception {
		}

	}

}
