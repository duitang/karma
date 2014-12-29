package com.duitang.service.karma.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import com.duitang.service.karma.transport.KarmaNettyDecoder;

public class KarmaHandlerInitializer extends ChannelInitializer<SocketChannel> {

	protected SimpleChannelInboundHandler handler;

	public KarmaHandlerInitializer() {
	}

	public KarmaHandlerInitializer(SimpleChannelInboundHandler handler) {
		this.handler = handler;
	}

	public void setHandler(SimpleChannelInboundHandler handler) {
		this.handler = handler;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipe = ch.pipeline();
		pipe.addLast("frame", new KarmaNettyDecoder());
		pipe.addLast("handler", handler);
	}

}
