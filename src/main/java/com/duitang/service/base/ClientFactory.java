package com.duitang.service.base;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public abstract class ClientFactory<T> implements ServiceFactory<T> {

	protected Logger err = Logger.getLogger("error");

	/**
	 * only 1 nio client socket channel factory because of leak risk
	 * 
	 * @see <a href="javatar.iteye.com/blog/1138527">netty内存泄漏</a>
	 */
	final static protected NioClientSocketChannelFactory cliFac = new NioClientSocketChannelFactory(
	        Executors.newCachedThreadPool(new NettyTransceiverThreadFactory("Avro "
	                + NettyTransceiver.class.getSimpleName() + " Boss")),
	        Executors.newCachedThreadPool(new NettyTransceiverThreadFactory("Avro "
	                + NettyTransceiver.class.getSimpleName() + " I/O Worker")));

	protected String url;
	protected List<URL> serviceURL;
	protected List<Boolean> serviceHTTPProtocol;
	protected AtomicInteger hashid = new AtomicInteger(0);
	protected int sz;
	protected int timeout = 500;
	protected String clientid;
	protected TraceableObject<T> tracer;

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
		tracer = new TraceableObject<T>();
	}

	protected void initClientName() {
		if (clientid == null) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			for (int i = 0; i < stacktrace.length; i++) {
				StackTraceElement e = stacktrace[i];
				if (e.getMethodName() != null && !e.getClassName().startsWith("com.duitang.service.base")) {
					clientid = MetricCenter.getHostname() + "|" + e.getFileName() + "@" + e.getLineNumber() + ":"
					        + e.getMethodName();
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
		MetricableHttpTransceiver.setTimeout(timeout);
	}

	@Override
	public T create() {
		T ret = null;
		try {
			Transceiver trans = null;
			if (sz == 0) {
				throw new RuntimeException("no remote url find? please setUrl(String url)");
			}
			int iid = hashid.incrementAndGet() % sz;
			URL u = serviceURL.get(iid);
			if (serviceHTTPProtocol.get(iid)) {
				trans = new MetricableHttpTransceiver(this.clientid, u);
			} else {
				trans = new NettyTransceiver(new InetSocketAddress(u.getHost(), u.getPort()), cliFac);
			}
			ret = (T) ReflectRequestor.getClient(getServiceType(), trans);
			ret = tracer.createTraceableInstance(ret, getServiceType(), clientid, null);
		} catch (IOException e) {
			err.error("create for service: " + this.url, e);
		}
		return ret;
	}

	public void release(T client) {
		if (client instanceof Closeable) {
			try {
				((Closeable) client).close();
			} catch (IOException e) {
				err.error(e);
			}
		}
	}

	protected static class NettyTransceiverThreadFactory implements ThreadFactory {
		private final AtomicInteger threadId = new AtomicInteger(0);
		private final String prefix;

		/**
		 * Creates a NettyTransceiverThreadFactory that creates threads with the
		 * specified name.
		 * 
		 * @param prefix
		 *            the name prefix to use for all threads created by this
		 *            ThreadFactory. A unique ID will be appended to this prefix
		 *            to form the final thread name.
		 */
		public NettyTransceiverThreadFactory(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName(prefix + " " + threadId.incrementAndGet());
			return thread;
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

}
