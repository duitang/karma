package com.duitang.service.karma.server;

import java.util.Date;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.router.JsonRouter;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.support.IPUtils;
import com.duitang.service.karma.transport.JsonServlet;

public class HTTPServer implements RPCService {

	final static int DEFAULT_PORT = 7777;

	protected int port;
	protected org.eclipse.jetty.server.Server server;
	protected JsonServlet servlet = new JsonServlet();
	protected Date created;
	protected String grp;

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

	@Override
	public void setRouter(Router router) {
		this.servlet.setRouter((JsonRouter) router);
	}

	@Override
	public void start() throws KarmaException {
		try {
			this.server = new org.eclipse.jetty.server.Server(this.port);
			this.server.setHandler(servlet);
			this.server.start();
			KarmaServerConfig.clusterAware.registerWrite(this);
			this.created = new Date();
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

	@Override
	public String getServiceURL() {
		String ret = null;
		try {
			ret = "http://" + IPUtils.pickUpIpNot("127.0.0.") + ":" + this.port;
		} catch (Exception e) {
			ret = "http://localhost:" + this.port;
		}
		return ret;
	}

	@Override
	public void setGroup(String grp) {
		this.grp = grp;
	}

	@Override
	public Date getUptime() {
		return created;
	}

	@Override
	public String getGroup() {
		return grp;
	}

	@Override
	public String getServiceProtocol() {
		return "http";
	}

	@Override
	public boolean online() {
		return server.isRunning();
	}

}
