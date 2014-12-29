package com.duitang.service.karma.transport;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.ByteBuffer;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;
import com.duitang.service.karma.router.Router;

@Sharable
public class JavaServerHandler extends SimpleChannelInboundHandler<BinaryPacketRaw> {

	protected Router<BinaryPacketRaw> router;

	public void setRouter(Router<BinaryPacketRaw> router) {
		this.router = router;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BinaryPacketRaw msg) throws Exception {
		BinaryPacketRaw raw = (BinaryPacketRaw) msg;
		raw.ctx = ctx;
		try {
			router.route(new RPCContext(), raw);
		} catch (Throwable e) {
			raw.setError(ByteBuffer.wrap(e.getMessage().getBytes()));
			BinaryPacketData data = null;
			try {
				data = BinaryPacketHelper.fromRawToData(raw);
			} catch (KarmaException e1) {
				data = new BinaryPacketData();
				data.ex = e;
			}
			ctx.write(data.getBytes()); // immediately error
		}
	}

}
