package com.duitang.service.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.List;

import com.duitang.service.meta.BinaryPacketData;

public class KarmaPacketCodec extends ByteToMessageCodec {

	static AttributeKey<KarmaPacketDecoderNetty> DECODER_NAME = AttributeKey.valueOf("DECODER");

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		BinaryPacketData packet = (BinaryPacketData) msg;
		ByteBuf buf = packet.getBytes();
		out.readBytes(buf);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {
		Attribute<KarmaPacketDecoderNetty> dd = ctx.attr(DECODER_NAME);
		KarmaPacketDecoderNetty dec = dd.get();
		dec.decode(ctx, in, out);
	}
}
