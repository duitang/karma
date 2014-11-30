package com.duitang.service.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.CallFuture;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.avro.ipc.Transceiver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class MinaTransceiver extends Transceiver {

	protected static CallbackCenter cbcenter = CallbackCenter.getInstance();
	protected static ConcurrentHashMap<String, ObjectPool<MinaTransceiver>> pool = new ConcurrentHashMap<String, ObjectPool<MinaTransceiver>>();

	protected NioSocketConnector connector;
	// protected Headquarter headquarter;
	protected ConnectFuture cf;
	protected Protocol remote;
	protected IoSession session;
	protected String remoteName;
	protected String url;

	static public MinaTransceiver getInstance(String url) throws Exception {
		if (!pool.containsKey(url)) {
			System.out.println("*******************************=>" + url );
			synchronized (pool) {
				if (!pool.contains(url)) {
					ObjectPool<MinaTransceiver> one = new GenericObjectPool<MinaTransceiver>(new MinaTranseiverFactory(
					        url));
					pool.putIfAbsent(url, one);
				}
			}
		}
		ObjectPool<MinaTransceiver> one = pool.get(url);
		return one.borrowObject();
	}

	void init(InetSocketAddress addr) {
		if (connector == null) {
			connector = new NioSocketConnector();
			connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new AvroCodecFactory()));
			connector.setHandler(Headquarter.handler);
			try {
				cf = connector.connect(addr).await();
			} catch (InterruptedException e) {
			}
		}
	}

	protected MinaTransceiver(String url, InetSocketAddress addr) throws IOException {
		this.url = url;
		init(addr);

		session = cf.getSession();
		remoteName = session.getRemoteAddress().toString();
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
		// if (this.remote == null) {
		// }
		// this.headquarter.setRemote(this.remote);
	}

	@Override
	public String getRemoteName() throws IOException {
		// return headquarter.getRemoteName();
		// return session.getRemoteAddress().toString();
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
			return transceiverFuture.get();
		} catch (InterruptedException e) {
			return null;
		} catch (ExecutionException e) {
			return null;
		}
	}

	@Override
	public void writeBuffers(List<ByteBuffer> buffers) throws IOException {
		NettyDataPack ndp = new NettyDataPack();
		ndp.setDatas(buffers);
		// ndp.setSerial(headquarter.getCallback().genId(buffers, null));
		// headquarter.write2Channel(ndp);
		ndp.setSerial(cbcenter.genId(buffers, null));
		write(ndp);
	}

	@Override
	public void transceive(List<ByteBuffer> request, Callback<List<ByteBuffer>> callback) throws IOException {
		NettyDataPack ndp = new NettyDataPack();
		ndp.setDatas(request);
		// ndp.setSerial(headquarter.getCallback().genId(request, callback));
		// headquarter.getCallback().push(ndp.getSerial(), callback);
		// headquarter.write2Channel(ndp);
		ndp.setSerial(cbcenter.genId(request, callback));
		cbcenter.push(ndp.getSerial(), callback);
		write(ndp);
	}

	protected void write(NettyDataPack ndp) {
		try {
			session.write(ndp).await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		ObjectPool<MinaTransceiver> one = pool.get(url);
		try {
			one.returnObject(this);
		} catch (Exception e) {
		}
	}

	public void release() throws IOException {
		session.close(true);
		cf.cancel();
		connector.dispose();
	}

	public boolean isAlive() {
		return session.isConnected();
	}

}
