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
		int iid = MinaEngine.rr.getAndIncrement();
		iid = Math.abs(iid) % MinaEngine.epoll_size;
		System.out.println("getEninge@MinaTransceiver get...." + iid);
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

	static protected String forceRemoteName(String hostAndPort) {
		String[] uu = hostAndPort.split(":");
		String host = uu[0];
		int port = Integer.valueOf(uu[1]);
		InetSocketAddress addr = new InetSocketAddress(host, port);
		return addr.toString();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public MinaTransceiver(String hostAndPort, long timeout) throws Exception{
		this.url = hostAndPort;
		String[] uu = hostAndPort.split(":");
		String host = uu[0];
		int port = Integer.valueOf(uu[1]);
		InetSocketAddress addr = new InetSocketAddress(host, port);
		this.remoteName = addr.toString();
		this.epoll = getEngine();
		System.out.println("about to connect " + addr);
		this.connection = this.epoll.epoll.connect(new InetSocketAddress(host, port));
		System.out.println("after to connect " + addr);
		if (!this.connection.await(timeout, TimeUnit.MILLISECONDS)) {
			this.lost = true;
			System.out.println("MinaTransceiver create connection to " + hostAndPort + " failed");
			throw new IOException("create connection to " + hostAndPort + " failed!");
		}
		this.session = connection.getSession();
		System.out.println("MinaTransceiver ok " + this);
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
		connection.cancel();
		session.close(true);
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
