package com.duitang.service.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

import com.duitang.service.KarmaException;
import com.duitang.service.router.Router;
import com.duitang.service.transport.JavaServerHandler;

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
		} catch (Exception e) {
			throw new KarmaException(e);
		}
	}

	public void stop() {
		try {
			boss.shutdownGracefully().sync();
		} catch (InterruptedException e) {
		}
		try {
			worker.shutdownGracefully().sync();
		} catch (InterruptedException e) {
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
