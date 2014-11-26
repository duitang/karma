package com.duitang.service.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.duitang.service.base.CallbackRepository;

public class Headquarter {

	static protected ConcurrentHashMap<String, Headquarter> mgr = new ConcurrentHashMap<String, Headquarter>();

	static protected CallbackRepository cbcenter = new CallbackCenter();
	static protected IoHandler handler = new AvroRPCHandler(cbcenter);

	protected NioSocketConnector connector;
	protected ConnectFuture conn;
	protected Protocol remote;
	protected String remoteName;
	protected IoSession session;

	public Protocol getRemote() {
		return remote;
	}

	public void setRemote(Protocol remote) {
		this.remote = remote;
	}

	public String getRemoteName() {
		return this.remoteName;
	}

	static public Headquarter getHeadquarter(String host, int port) throws IOException {
		String k = host + ":" + port;
		Headquarter ret = mgr.get(k);
		if (ret != null) {
			return ret;
		}

		synchronized (mgr) {
			if (mgr.contains(k)) {
				return mgr.get(k);
			}
			ret = new Headquarter();
			ret.remoteName = k;
			ret.connector = new NioSocketConnector();
			ret.connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new AvroCodecFactory()));
			ret.connector.setHandler(Headquarter.handler);
			InetSocketAddress addr = new InetSocketAddress(host, port);
			try {
				ret.conn = ret.connector.connect(addr).await();
				ret.session = ret.conn.getSession();
			} catch (InterruptedException e1) {
				if (!ret.conn.isConnected()) {
					ret.conn = null;
				}
			}
			try {
				ret.conn.await(3000);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			mgr.putIfAbsent(k, ret);
		}
		return ret;
	}

	public void request(List<ByteBuffer> data, Callback<List<ByteBuffer>> cb) {
		int uuid = cbcenter.genId(data, cb);
		NettyDataPack dataPack = new NettyDataPack(uuid, data);
		cbcenter.push(uuid, cb);

//		try {
			session.write(dataPack);
			// if (wf.)
			// for(int i=0; i< 100; i++){
			// System.out.println(wf.isDone() + "!!");
			// System.out.println(wf.isWritten() + "^^");
			// Thread.sleep(10);
			// }
			// wf.await();

//		} catch (InterruptedException e) {
//		}
		// session.close(false);
		// return cbcenter.pop(uuid);
	}

	protected void active() {
		if (session == null) {
			synchronized (this) {
				if (session == null) {
					session = conn.getSession();
					System.out.println("addd---->s");
				}
			}
		}
	}
	
}
