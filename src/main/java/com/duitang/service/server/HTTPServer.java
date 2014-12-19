package com.duitang.service.server;

import org.eclipse.jetty.server.handler.AbstractHandler;

import com.duitang.service.KarmaException;

public class HTTPServer implements RPCService {

	final static int DEFAULT_PORT = 7777;

	protected int port;
	protected org.eclipse.jetty.server.Server server;
	protected AbstractHandler router;

	public HTTPServer() {
		this(DEFAULT_PORT);
	}

	public HTTPServer(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public AbstractHandler getRouter() {
		return router;
	}

	public void setRouter(AbstractHandler router) {
		this.router = router;
	}

	@Override
	public void start() throws KarmaException {
		try {
			this.server = new org.eclipse.jetty.server.Server(port);
			this.server.setHandler(router);
			this.server.start();
		} catch (Exception e) {
			throw new KarmaException(e);
		}
	}

	@Override
	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
