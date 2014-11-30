package com.duitang.service.mina;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.duitang.service.base.CallbackRepository;

public class Headquarter implements Closeable {

	static protected ConcurrentHashMap<String, Headquarter> mgr = new ConcurrentHashMap<String, Headquarter>();

	static protected CallbackRepository cbcenter = CallbackCenter.getInstance();
	static protected IoHandler handler = new AvroRPCHandler(cbcenter);

	protected NioSocketConnector connector;
	protected ConnectFuture conn;
	protected Protocol remote;
	protected String remoteName;
	protected ObjectPool<IoSession> pool;

	// protected List<IoSession> pool;

	public CallbackRepository getCallback() {
		return cbcenter;
	}

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
				ret.pool = new GenericObjectPool<IoSession>(new IoSessionFactory(ret.conn));
			} catch (InterruptedException e1) {
				if (!ret.conn.isConnected()) {
					ret.conn = null;
				}
			}
			// ret.pool = new ArrayList<IoSession>();
			// try {
			// ret.conn.await(3000);
			// for (int i = 0; i < 10; i++) {
			// ret.pool.add(ret.conn.getSession());
			// }
			// } catch (InterruptedException e) {
			// throw new IOException(e);
			// }
			// ret.iidsz = ret.pool.size();
			mgr.putIfAbsent(k, ret);
		}
		return ret;
	}

	public void close() {
		// this.pool.close();
		this.connector.dispose();
		this.conn.cancel();
	}

	// public void write2Channel(List<ByteBuffer> data,
	// CallFuture<List<ByteBuffer>> cf) {
	// if (data == null || cf == null) {
	// return;
	// }
	// NettyDataPack ndp = new NettyDataPack();
	// ndp.setSerial(cbcenter.genId(data, cf));
	// ndp.setDatas(data);
	// cbcenter.push(ndp.getSerial(), cf);
	// IoSession session = null;
	// try {
	// session = pool.borrowObject();
	// session.write(ndp);
	// } catch (Exception e) {
	// } finally {
	// if (session != null) {
	// try {
	// pool.returnObject(session);
	// } catch (Exception e) {
	// }
	// }
	// }
	// try {
	// cf.await();
	// } catch (InterruptedException e) {
	// }
	// }

	public void write2Channel(NettyDataPack datapack) {
		if (datapack == null) {
			return;
		}
		IoSession session = null;
		WriteFuture wf = null;
		try {
			session = pool.borrowObject();
			wf = session.write(datapack);
			try {
				if (wf != null) {
					wf.await();
				}
			} catch (InterruptedException e) {
			}
			Thread.sleep(10);
		} catch (Exception e) {
		} finally {
			if (session != null) {
				try {
					pool.returnObject(session);
				} catch (Exception e) {
				}
			}
		}
	}

}
