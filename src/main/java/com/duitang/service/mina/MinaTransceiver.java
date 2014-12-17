package com.duitang.service.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.CallFuture;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.avro.ipc.Transceiver;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import com.duitang.service.base.Validation;

/**
 * for safety consideration, no connection, no session reused
 * 
 * @author laurence
 * 
 */
public class MinaTransceiver extends Transceiver implements Validation {

	static final protected long default_timeout = 500; // 0.5s
	static final protected List<MinaEpoll> engine = new ArrayList<MinaEpoll>();
	static final protected int epoll_size = 4;
	// round robin
	static final protected AtomicInteger rr = new AtomicInteger(0);

	static {
		for (int i = 0; i < epoll_size; i++) {
			MinaEpoll m = new MinaEpoll();
			m.epoll.getSessionConfig().setTcpNoDelay(true);
			m.epoll.getSessionConfig().setKeepAlive(true);
			m.epoll.getFilterChain().addLast("codec", new ProtocolCodecFilter(new AvroCodecFactory()));
			m.epoll.setHandler(new MinaRPCHandler(m));
			engine.add(m);
		}
	}

	static public MinaEpoll getEngine() {
		int iid = rr.getAndIncrement();
		iid = Math.abs(iid) % epoll_size;
		return engine.get(iid);
	}

	protected String remoteName;
	protected String url;
	protected long timeout = default_timeout;

	protected MinaEpoll epoll;
	protected ConnectFuture connection;
	protected IoSession session;
	protected Protocol remote;
	protected boolean lost = false;

	// not thread-safe
	protected boolean initialed = false;

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public MinaTransceiver(String hostAndPort, long timeout) {
		this.url = hostAndPort;
		this.timeout = timeout;
		String[] uu = url.split(":");
		String host = uu[0];
		int port = Integer.valueOf(uu[1]);
		InetSocketAddress addr = new InetSocketAddress(host, port);
		this.remoteName = addr.toString();
		this.epoll = getEngine();
		this.connection = this.epoll.epoll.connect(new InetSocketAddress(host, port));
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
			this.lost = true;
			this.close();
			throw new IOException("create connection to " + url + " failed!");
		}
		this.session = connection.getSession();
		this.initialed = true;
		return;
	}

	@Override
	public boolean isConnected() {
		return getRemote() != null;
	}

	@Override
	public Protocol getRemote() {
		return remote;
	}

	@Override
	public void setRemote(Protocol protocol) {
		this.remote = protocol;
	}

	@Override
	public String getRemoteName() throws IOException {
		return this.remoteName;
	}

	@Override
	public List<ByteBuffer> readBuffers() throws IOException {
		return null;
	}

	@Override
	public List<ByteBuffer> transceive(List<ByteBuffer> request) throws IOException {
		try {
			CallFuture<List<ByteBuffer>> transceiverFuture = new CallFuture<List<ByteBuffer>>();
			transceive(request, transceiverFuture);
			return transceiverFuture.get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			this.lost = true;
		}
		return null;
	}

	@Override
	public void writeBuffers(List<ByteBuffer> buffers) throws IOException {
		write(buffers, null);
	}

	@Override
	public void transceive(List<ByteBuffer> request, Callback<List<ByteBuffer>> callback) throws IOException {
		write(request, callback);
	}

	protected void write(List<ByteBuffer> buffers, Callback<List<ByteBuffer>> callback) {
		CallFuture<List<ByteBuffer>> gw = new CallFuture<List<ByteBuffer>>(callback);
		NettyDataPack ndp = new NettyDataPack();
		ndp.setDatas(buffers);
		int uuid = epoll.uuid.incrementAndGet();
		ndp.setSerial(uuid);
		epoll.callbacks.put(uuid, gw);
		session.write(getPackHeader(ndp));
		for (ByteBuffer d : ndp.getDatas()) {
			session.write(getLengthHeader(d));
			session.write(IoBuffer.wrap(d.array(), d.position(), d.remaining()));
		}
		try {
			gw.get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			lost = true;
			gw.handleError(null);
		}
	}

	@Override
	public void close() throws IOException {
		// System.out.println("return mina socket: " + socket);
		if (connection != null) {
			connection.cancel();
		}
		if (session != null) {
			session.close(true);
		}
	}

	private IoBuffer getPackHeader(NettyDataPack dataPack) {
		IoBuffer ret = IoBuffer.allocate(8);
		ret.putInt(dataPack.getSerial());
		ret.putInt(dataPack.getDatas().size());
		return ret.flip();
	}

	private IoBuffer getLengthHeader(ByteBuffer buf) {
		IoBuffer ret = IoBuffer.allocate(4);
		ret.putInt(buf.limit());
		return ret.flip();
	}

	@Override
	public boolean isValid() {
		return !lost && connection.isConnected();
	}

}
