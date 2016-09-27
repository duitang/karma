package com.duitang.service.karma.server;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.support.NameUtil;
import com.duitang.service.karma.transport.JavaServerHandler;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class TCPServer implements RPCService, GenericFutureListener {

	final static int DEFAULT_TCP_PORT = 7778;
	static KarmaHandlerInitializer starter;

	protected NioEventLoopGroup boss;
	protected NioEventLoopGroup worker;
	protected ServerBootstrap boot;

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
		JavaServerHandler handler = new JavaServerHandler();
		handler.setRouter(router);
		starter = new KarmaHandlerInitializer(handler);

		boss = new NioEventLoopGroup();
		worker = new NioEventLoopGroup();
		boot = new ServerBootstrap();
		boot.group(boss, worker).channel(NioServerSocketChannel.class);
		boot.option(ChannelOption.TCP_NODELAY, true);
		boot.option(ChannelOption.SO_BACKLOG, 3000);
		boot.childOption(ChannelOption.SO_KEEPALIVE, true);
		boot.childHandler(starter);
		try {
			ChannelFuture cf = boot.bind(new InetSocketAddress(port)).sync();
			cf.channel().closeFuture().addListener(this);
			router.setHostInfo(NameUtil.getHostname(), port);
			KarmaServerConfig.updateHostInfo(NameUtil.getInstanceTag().ipv4, port);
		} catch (Exception e) {
			throw new KarmaException(e);
		}
	}

	public void stop() {
		try {
			boss.shutdownGracefully().await(1000);// sync();
		} catch (InterruptedException e) {
			// ignored
		}
		try {
			worker.shutdownGracefully().await(1000);// .sync();
		} catch (InterruptedException e) {
			// ignored
		}
	}

	@Override
	public void setRouter(Router router) {
		this.router = router;
	}

	public void operationComplete(Future future) throws Exception {
		stop();
	}

}
