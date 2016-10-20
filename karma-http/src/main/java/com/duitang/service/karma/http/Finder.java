package com.duitang.service.karma.http;

import java.util.concurrent.ConcurrentHashMap;

import com.duitang.service.karma.boot.KarmaFinder;
import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.handler.JsonRPCHandler;
import com.duitang.service.karma.router.JsonRouter;
import com.duitang.service.karma.server.CoreEnhanced;
import com.duitang.service.karma.server.HTTPServer;

public class Finder implements KarmaFinder {

	static CoreEnhancedImpl enhancer = new CoreEnhancedImpl();

	static class CoreEnhancedImpl implements CoreEnhanced {

		protected ServerBootstrap core;

		@Override
		public void enhanced(ServerBootstrap server) {
			core = server;
		}

	};

	final static ConcurrentHashMap<Integer, HTTPServer> http = new ConcurrentHashMap<>();

	@Override
	public <T> T find(Class<T> clazz) {
		return (T) enhancer;
	}

	/**
	 * enable HTTP port for core service engine
	 * 
	 * @param port
	 * @throws Exception
	 */
	synchronized public static void enableHTTPService(int port) throws Exception {
		if (http.containsKey(Integer.valueOf(port))) {
			return;
		}

		HTTPServer httpServer = new HTTPServer(port);
		JsonRPCHandler rpc1 = new JsonRPCHandler(enhancer.core.getCoreHandler());
		JsonRouter jsonRouter = new JsonRouter();
		jsonRouter.setHandler(rpc1);
		httpServer.setRouter(jsonRouter);
		httpServer.start();
		KarmaServerConfig.clusterAware.registerWrite(httpServer);

		http.putIfAbsent(Integer.valueOf(port), httpServer);
	}

	synchronized public static void disableHTTPService(int port) throws Exception {
		HTTPServer server = http.remove(Integer.valueOf(port));
		if (server == null) {
			return;
		}
		KarmaServerConfig.clusterAware.unRegisterWrite(server);
		server.stop();
	}

	public static HTTPServer getHTTPServer(int port) {
		return http.get(Integer.valueOf(port));
	}

}
