package com.duitang.service.base;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Provider;

import org.apache.thrift.TProcessor;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.facebook.nifty.core.NettyServerConfig;
import com.facebook.nifty.core.NettyServerConfigBuilder;
import com.facebook.nifty.core.NettyServerTransport;
import com.facebook.nifty.core.ThriftServerDef;
import com.facebook.nifty.core.ThriftServerDefBuilder;

public class ServerBootstrap {

	protected TProcessor processor;
	protected ThriftServerDef serverDef;
	protected NettyServerTransport server;

	protected ExecutorService bossExecutor;
	protected ExecutorService workerExecutor;

	public void startUp(TProcessor processor, int port) throws IOException {
		// Build the server definition
		serverDef = new ThriftServerDefBuilder().withProcessor(processor).listen(port).build();

		// Create the server transport
		server = new NettyServerTransport(serverDef, new NettyConfigProvider().get(), new DefaultChannelGroup());

		// Create netty boss and executor thread pools
		bossExecutor = Executors.newCachedThreadPool();
		workerExecutor = Executors.newCachedThreadPool();

		// Start the server
		server.start(new NioServerSocketChannelFactory(bossExecutor, workerExecutor));

		// Arrange to stop the server at shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					server.stop();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});

	}

	public static class NettyConfigProvider implements Provider<NettyServerConfig> {
		@Override
		public NettyServerConfig get() {
			NettyServerConfigBuilder nettyConfigBuilder = new NettyServerConfigBuilder();
			nettyConfigBuilder.getSocketChannelConfig().setTcpNoDelay(true);
			nettyConfigBuilder.getSocketChannelConfig().setConnectTimeoutMillis(5000);
			return nettyConfigBuilder.build();
		}
	}

	public void shutdown() {
		if (server != null) {
			server.getServerChannel().close();
		}
	}

}
