package com.duitang.service.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;

public class NettyFrameEncoder extends MessageToByteEncoder<NettyDataPack> {

	private void getPackHeader(NettyDataPack dataPack, ByteBuf out) {
		out.writeInt(dataPack.getSerial());
		out.writeInt(dataPack.getDatas().size());
	}

	private void getLengthHeader(ByteBuffer buf, ByteBuf out) {
		out.writeInt(buf.limit());
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, NettyDataPack msg, ByteBuf out) throws Exception {
		NettyDataPack dataPack = (NettyDataPack) msg;
		List<ByteBuffer> origs = dataPack.getDatas();
		// prepend a pack header including serial number and list size
		getPackHeader(dataPack, out);
		for (ByteBuffer b : origs) {
			// for each buffer prepend length field
			getLengthHeader(b, out);
			out.writeBytes(b);
		}
	}

}
