package com.duitang.service.base;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.codecs.NettyFrameDecoder;
import com.duitang.service.codecs.NettyFrameEncoder;

/**
 * A Netty-based RPC {@link Server} implementation.
 */
public class SmartNettyServer implements Server {
	private static final Logger LOG = LoggerFactory.getLogger(NettyServer.class.getName());

	protected final Responder responder;

	protected ServerBootstrap server;
	protected Channel serverChannel;
	protected InetSocketAddress addr;
	protected final CountDownLatch closed = new CountDownLatch(1);

	protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2; // 默认
	/** 业务出现线程大小 */
	protected static final int BIZTHREADSIZE = 60;
	protected static EventLoopGroup bossGroup;
	protected static EventLoopGroup workerGroup;

	public SmartNettyServer(Responder responder, InetSocketAddress addr) throws Exception {
		this(responder, addr, new NioEventLoopGroup(BIZGROUPSIZE), new NioEventLoopGroup(BIZTHREADSIZE));
	}

	public SmartNettyServer(Responder responder, InetSocketAddress addr, EventLoopGroup boss, EventLoopGroup workers)
	        throws Exception {
		this.responder = responder;
		bossGroup = boss;
		workerGroup = workers;
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast("frameDecoder", new NettyFrameDecoder());
				p.addLast("frameEncoder", new NettyFrameEncoder());
				p.addLast("handler", new SmartNettyServerAvroHandler());
			}

		});

		server = bootstrap;
		serverChannel = bootstrap.bind(addr).sync().channel();
	}

	@Override
	public void start() {
		// No-op.
	}

	@Override
	public void close() {
		ChannelFuture future = serverChannel.close();
		future.awaitUninterruptibly();
		closed.countDown();
	}

	@Override
	public int getPort() {
		return addr.getPort();
	}

	@Override
	public void join() throws InterruptedException {
		closed.await();
	}

	/**
	 * Avro server handler for the Netty transport
	 */
	class SmartNettyServerAvroHandler extends SimpleChannelInboundHandler<NettyDataPack> {

		private SmartNettyTransceiver connectionMetadata = new SmartNettyTransceiver();

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, NettyDataPack msg) throws Exception {
			try {
				NettyDataPack dataPack = msg;
				List<ByteBuffer> req = dataPack.getDatas();
				List<ByteBuffer> res = responder.respond(req, connectionMetadata);
				// response will be null for oneway messages.
				if (res != null) {
					dataPack.setDatas(res);
					ctx.channel().writeAndFlush(dataPack);
				}
			} catch (IOException ex) {
				LOG.warn("unexpect error");
			}
		}

	}
}
