package com.duitang.service.karma.server;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.support.IPUtils;
import com.duitang.service.karma.support.NameUtil;
import com.duitang.service.karma.transport.JavaServerHandler;

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

	protected Date created;
	protected String grp;
	protected boolean online = false;

	static class DaemonThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		DaemonThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "server-pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			t.setDaemon(true);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			System.err.println(t.getName());
			return t;
		}
	}

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

		boss = new NioEventLoopGroup(0, new DaemonThreadFactory());
		worker = new NioEventLoopGroup(0, new DaemonThreadFactory());
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
			created = new Date();
			online = true;
		} catch (Exception e) {
			online = false;
			throw new KarmaException(e);
		}
	}

	public void stop() {
		online = false;
		try {
			boss.shutdownGracefully().await(KarmaServerConfig.KARMA_SERVER_SHUTDOWN_TIMEOUT);// sync();
		} catch (InterruptedException e) {
			// ignored
		}
		try {
			worker.shutdownGracefully().await(KarmaServerConfig.KARMA_SERVER_SHUTDOWN_TIMEOUT);// .sync();
		} catch (InterruptedException e) {
			// ignored
		}
		boss = null;
		worker = null;
	}

	@Override
	public void setRouter(Router router) {
		this.router = router;
	}

	public void operationComplete(Future future) throws Exception {
		stop();
	}

	@Override
	public String getServiceURL() {
		String ret = null;
		try {
			ret = "tcp://" + IPUtils.pickUpIpNot("127.0.0.") + ":" + this.port;
		} catch (Exception e) {
			ret = "tcp://localhost:" + this.port;
		}
		return ret;
	}

	@Override
	public String getServiceProtocol() {
		return "tcp";
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
	public boolean online() {
		return online;
	}

}
