package com.duitang.service.transport;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

import com.duitang.service.client.KarmaRemoteLatch;
import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.meta.BinaryPacketHelper;
import com.duitang.service.meta.BinaryPacketRaw;

public class JavaClientHandler extends SimpleChannelInboundHandler<BinaryPacketRaw> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BinaryPacketRaw msg) throws Exception {
		BinaryPacketRaw raw = (BinaryPacketRaw) msg;
		BinaryPacketData data = null;
		data = BinaryPacketHelper.fromRawToData(raw);
		Attribute<KarmaRemoteLatch> att = ctx.channel().attr(KarmaRemoteLatch.LATCH_KEY);
		KarmaRemoteLatch latch = att.get();
		if (data.ex != null) {
			latch.offerError(data.ex);
		} else {
			latch.offerResult(data.ret);
		}
	}

}
