package com.duitang.service.karma.http;

import com.duitang.service.karma.boot.KarmaFinder;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.handler.JsonRPCHandler;
import com.duitang.service.karma.router.JsonRouter;
import com.duitang.service.karma.server.CoreEnhanced;
import com.duitang.service.karma.server.HTTPServer;

public class Finder implements KarmaFinder {

	static ServerBootstrap core;
	static CoreEnhanced enhancer = new CoreEnhanced() {

		@Override
		public void enhanced(ServerBootstrap server) {
			Finder.core = server;
		}

	};

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
	public static void enableHTTPService(int port) throws Exception {
		HTTPServer http = new HTTPServer(port);
		JsonRPCHandler rpc1 = new JsonRPCHandler(core.getCoreHandler());
		JsonRouter jsonRouter = new JsonRouter();
		jsonRouter.setHandler(rpc1);
		http.setRouter(jsonRouter);
		http.start();
	}

}
