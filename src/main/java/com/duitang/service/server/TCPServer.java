package com.duitang.service.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.core.service.SimpleIoProcessorPool;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.duitang.service.KarmaException;
import com.duitang.service.router.Router;
import com.duitang.service.transport.JavaServerHandler;
import com.duitang.service.transport.KarmaBinaryCodecFactory;

public class TCPServer implements RPCService {

	final static int DEFAULT_TCP_PORT = 7778;
	protected Executor pool1 = Executors.newFixedThreadPool(100);
	protected SimpleIoProcessorPool proc = new SimpleIoProcessorPool(NioProcessor.class, pool1);
	protected SocketAcceptor acceptor = new NioSocketAcceptor(pool1, proc);
	protected Router router;

	protected int port = DEFAULT_TCP_PORT;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void start() throws KarmaException {
		acceptor.setReuseAddress(true);
		acceptor.getSessionConfig().setTcpNoDelay(true);
		acceptor.getSessionConfig().setKeepAlive(true);
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new KarmaBinaryCodecFactory()));
		JavaServerHandler handler = new JavaServerHandler();
		handler.setRouter(router);
		acceptor.setHandler(handler);
		try {
			acceptor.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			throw new KarmaException(e);
		}
	}

	@Override
	public void stop() {
		acceptor.dispose();
	}

	@Override
	public void setRouter(Router router) {
		this.router = router;
	}

}
