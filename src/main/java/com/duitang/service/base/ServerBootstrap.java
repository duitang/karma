package com.duitang.service.base;

import java.io.IOException;

import org.apache.avro.ipc.HttpServer;
import org.apache.avro.ipc.specific.SpecificResponder;

public class ServerBootstrap {

	protected HttpServer server;

	public void startUp(Class serviceType, Object service, int port) throws IOException {
		server = new HttpServer(new SpecificResponder(serviceType, service), 9090);
		server.start();
	}

	public void shutdown() {
		if (server != null) {
			server.close();
		}
	}

}
