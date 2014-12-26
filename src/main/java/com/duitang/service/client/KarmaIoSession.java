package com.duitang.service.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.duitang.service.base.LifeCycle;
import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.transport.JavaClientHandler;
import com.duitang.service.transport.KarmaBinaryCodecFactory;

/**
 * for safety consideration, no connection, no session reused
 * 
 * @author laurence
 * 
 */
public class KarmaIoSession implements LifeCycle {

	static final protected long default_timeout = 500; // 0.5s

	static final protected Executor pool = Executors.newFixedThreadPool(100);
	static final protected SimpleIoProcessorPool proc = new SimpleIoProcessorPool(NioProcessor.class, pool);

	protected String url;
	protected long timeout = default_timeout;

	protected NioSocketConnector conn;
	protected ConnectFuture connection;
	protected IoSession session;

	protected volatile int errorCount = 0;

	// not thread-safe
	protected boolean initialed = false;

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void reportError() {
		this.errorCount++;
	}

	public KarmaIoSession(String hostAndPort, long timeout) {
		this.url = hostAndPort;
		this.timeout = timeout;
		conn = new NioSocketConnector(pool, proc);
		conn.getSessionConfig().setTcpNoDelay(true);
		conn.getSessionConfig().setKeepAlive(true);
		conn.getFilterChain().addLast("codec", new ProtocolCodecFilter(new KarmaBinaryCodecFactory()));
		conn.setHandler(new JavaClientHandler());
		String[] uu = url.split(":");
		String host = uu[0];
		int port = Integer.valueOf(uu[1]).intValue();
		this.connection = this.conn.connect(new InetSocketAddress(host, port));
	}

	public void init() throws IOException {
		if (initialed) {
			return;
		}
		try {
			// ensure connect stable, should > 1s
			// so connect is very heavy action
			long t = timeout >= 1000 ? timeout : 1000;
			this.connection.await(t);
		} catch (InterruptedException e) {
		}

		if (!this.connection.isConnected()) {
			this.close();
			throw new IOException("create connection to " + url + " failed!");
		}
		this.session = connection.getSession();
		this.initialed = true;
	}

	public boolean isConnected() {
		return this.session.isConnected();
	}

	public void write(BinaryPacketData data) {
		this.session.write(data.getBytes());
	}

	public void setAttribute(KarmaRemoteLatch latch) {
		this.session.setAttribute(KarmaRemoteLatch.LATCH_NAME, latch);
	}

	@Override
	public void close() throws IOException {
		// System.out.println("return mina socket: " + socket);
		if (session != null) {
			try {
				session.close(true).await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (connection != null) {
			connection.cancel();
		}
		if (conn != null) {
			conn.dispose(true);
		}
	}

	@Override
	public boolean isAlive() {
		return errorCount < 5 && conn.isActive();
	}

}
