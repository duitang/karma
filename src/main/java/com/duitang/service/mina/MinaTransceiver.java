package com.duitang.service.mina;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.CallFuture;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.avro.ipc.Transceiver;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * for safety consideration, no connection, no session reused
 * 
 * @author laurence
 * 
 */
public class MinaTransceiver extends Transceiver {

	static final protected long default_timeout = 500; // 0.5s

	static protected Field vistor;

	protected MinaSocket socket;
	protected String remoteName;
	protected String url;
	protected long timeout = default_timeout;
	protected int uuid;

	static {
		try {
			vistor = CallFuture.class.getDeclaredField("latch");
		} catch (Exception e) {
			vistor = null; // not happen when version is defined
		}
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public MinaTransceiver(String host, int port) {
		this(host + ":" + port);
	}

	public MinaTransceiver(String hostAndPort) {
		this.url = hostAndPort;
		this.remoteName = ConnectionPool.getRemoteAddress(hostAndPort);
	}

	@Override
	public boolean isConnected() {
		return getRemote() != null;
	}

	@Override
	public Protocol getRemote() {
		// System.out.println("get remote: " + socket);
		if (socket == null) {
			// return null;
			socket = ConnectionPool.getConnection(url);
		}
		return socket.remote;
	}

	@Override
	public void setRemote(Protocol protocol) {
		if (socket == null) {
			socket = ConnectionPool.getConnection(url);
		}
		this.socket.remote = protocol;
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
			this.socket.lost = true;
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
		if (socket == null) {
			socket = ConnectionPool.getConnection(url);
		}
		CallFuture<List<ByteBuffer>> gw = new CallFuture<List<ByteBuffer>>(callback);
		NettyDataPack ndp = new NettyDataPack();
		ndp.setDatas(buffers);
		uuid = socket.epoll.uuid.incrementAndGet();
		ndp.setSerial(uuid);
		socket.epoll.callbacks.put(uuid, gw);
		// try {
		// System.out.println(session.toString());
		// System.out.println(socket.cf.getSession().toString());

		// System.out.println("write netty data pack ---> " + uuid);
		// socket.session.write(ndp);
		// } catch (InterruptedException e) {
		// }
		socket.session.write(getPackHeader(ndp));
		for (ByteBuffer d : ndp.getDatas()) {
			socket.session.write(getLengthHeader(d));
			socket.session.write(IoBuffer.wrap(d.array(), d.position(), d.remaining()));
		}

		try {
			gw.get(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			socket.lost = true;
			gw.handleError(e);
		}
	}

	@Override
	public void close() throws IOException {
		// System.out.println("return mina socket: " + socket);
		ConnectionPool.retConnection(url, socket);
		this.socket = null;
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

}
