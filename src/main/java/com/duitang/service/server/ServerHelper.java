package com.duitang.service.server;

import com.duitang.service.KarmaException;
import com.duitang.service.demo.DemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.handler.JsonRPCHandler;
import com.duitang.service.handler.ReflectRPCHandler;
import com.duitang.service.router.JsonRouter;
import com.duitang.service.router.Router;

public class ServerHelper implements RPCService {

	protected ServiceConfig conf;
	protected ReflectRPCHandler rpc;
	protected int httpPort = 7777;
	protected int tcpPort = 7778;

	protected HTTPServer httpServer;
	protected TCPServer tcpServer;

	@Override
	public void setRouter(Router router) {
		// not supported
	}

	public ServiceConfig getConf() {
		return conf;
	}

	public void setConf(ServiceConfig conf) {
		this.conf = conf;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public void start() throws KarmaException {
		rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();
		initHTTPRouter();
	}

	public void stop() {
		httpServer.stop();
	}

	public void initHTTPRouter() throws KarmaException {
		JsonRPCHandler jhl = new JsonRPCHandler(rpc);
		JsonRouter rt = new JsonRouter();
		rt.setHandler(jhl);
		httpServer = new HTTPServer(httpPort);
		httpServer.setRouter(rt);
		httpServer.start();
	}

	public static void main(String[] args) throws KarmaException {
		ServerHelper helper = new ServerHelper();
		ServiceConfig conf = new ServiceConfig();
		conf.addService(DemoService.class, new MemoryCacheService());
		helper.setConf(conf);
		helper.start();
	}

}
