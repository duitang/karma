package com.duitang.service.mina;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

public class ConnectionPool {

	static final protected List<MinaEpoll> epoll = new ArrayList<MinaEpoll>();
	static final protected int epoll_size = 4;
	// round robin
	static final protected AtomicInteger rr = new AtomicInteger(0);
	static final protected ConcurrentHashMap<String, GenericObjectPool<MinaSocket>> u2c = new ConcurrentHashMap<String, GenericObjectPool<MinaSocket>>();
	static final protected ConcurrentHashMap<String, String> u2addr = new ConcurrentHashMap<String, String>();

	static final protected long default_timeout = 500; // 0.5s

	static {
		for (int i = 0; i < epoll_size; i++) {
			MinaEpoll m = new MinaEpoll();
			m.epoll.getSessionConfig().setTcpNoDelay(true);
			m.epoll.getSessionConfig().setKeepAlive(true);
			m.epoll.getFilterChain().addLast("codec", new ProtocolCodecFilter(new AvroCodecFactory()));
			m.epoll.setHandler(new MinaRPCHandler(m));
			epoll.add(m);
		}
	}

	static public MinaSocket getConnection(String host) {
		return getConnection(host, default_timeout);
	}

	static public MinaSocket getConnection(String host, long timeout) {
		GenericObjectPool<MinaSocket> pool = u2c.get(host);
		if (pool == null) {
			pool = forceCreatePool(host);
		}
		try {
			return pool.borrowObject(timeout);
		} catch (Exception e) {
		}
		return null;
	}

	static public void retConnection(String host, MinaSocket msocket) {
		if (msocket == null) {
			return;
		}
		if (host == null || msocket.lost) { // no target
			msocket.session.close(true);
			GenericObjectPool<MinaSocket> pool = u2c.get(host);
			if (pool != null) {
				try {
					pool.invalidateObject(msocket);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return;
		}
		GenericObjectPool<MinaSocket> pool = u2c.get(host);
		if (pool == null) {
			pool = forceCreatePool(host);
		}
		pool.returnObject(msocket);
	}

	static public String getRemoteAddress(String hostAndPort) {
		String ret = u2addr.get(hostAndPort);
		if (ret == null) {
			ret = forceRemoteName(hostAndPort);
		}
		return ret;
	}

	static protected GenericObjectPool<MinaSocket> forceCreatePool(String host) {
		GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
		cfg.setMaxIdle(10);
		cfg.setMinIdle(3);
		cfg.setMaxTotal(200);
		cfg.setTestWhileIdle(false);
		cfg.setBlockWhenExhausted(true);
		GenericObjectPool<MinaSocket> newone = new GenericObjectPool<MinaSocket>(new MinaCFFactory(host), cfg);
		u2c.putIfAbsent(host, newone);
		return u2c.get(host);
	}

	static protected String forceRemoteName(String hostAndPort) {
		String[] uu = hostAndPort.split(":");
		String host = uu[0];
		int port = Integer.valueOf(uu[1]);
		InetSocketAddress addr = new InetSocketAddress(host, port);
		String ret = addr.toString();
		u2addr.putIfAbsent(hostAndPort, ret);
		return ret;
	}
}

class MinaCFFactory implements PooledObjectFactory<MinaSocket> {

	protected String host;
	protected int port;

	public MinaCFFactory(String host) {
		String[] uu = host.split(":");
		this.host = uu[0];
		this.port = Integer.valueOf(uu[1]);
	}

	@Override
	public PooledObject<MinaSocket> makeObject() throws Exception {
		try {
			int iid = ConnectionPool.rr.getAndIncrement();
			iid = Math.abs(iid) % ConnectionPool.epoll_size;
			MinaEpoll me = ConnectionPool.epoll.get(iid);
			MinaSocket ret = new MinaSocket(me);
			ret.connection = me.epoll.connect(new InetSocketAddress(host, port));
			ret.connection.await(ConnectionPool.default_timeout, TimeUnit.MILLISECONDS);
			ret.session = ret.connection.getSession();
			return new DefaultPooledObject<MinaSocket>(ret);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void destroyObject(PooledObject<MinaSocket> p) throws Exception {
		if (p == null) {
			return;
		}
		MinaSocket msocket = p.getObject();
		if (msocket != null) {
			// System.out.println("destroy MinaSocket: " + msocket);
			msocket.connection.cancel();
			msocket.session.close(true);
		}
	}

	@Override
	public boolean validateObject(PooledObject<MinaSocket> p) {
		MinaSocket ms = p.getObject();
		return !ms.lost && ms.connection.isConnected();
	}

	@Override
	public void activateObject(PooledObject<MinaSocket> p) throws Exception {
	}

	@Override
	public void passivateObject(PooledObject<MinaSocket> p) throws Exception {
	}

}
