package com.duitang.service.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;

public class NettyFrameDecoder extends ByteToMessageDecoder {

	private boolean packHeaderRead = false;
	private int listSize;
	private NettyDataPack dataPack;

	private boolean decodePackHeader(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		if (buffer.readableBytes() < 8) {
			return false;
		}

		int serial = buffer.readInt();
		int listSize = buffer.readInt();

		this.listSize = listSize;
		dataPack = new NettyDataPack(serial, new ArrayList<ByteBuffer>(listSize));

		return true;
	}

	private boolean decodePackBody(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		if (in.readableBytes() < 4) {
			return false;
		}

		in.markReaderIndex();

		int length = in.readInt();

		if (in.readableBytes() < length) {
			in.resetReaderIndex();
			return false;
		}

		ByteBuffer bb = ByteBuffer.allocate(length);
		in.readBytes(bb);
		bb.flip();
		dataPack.getDatas().add(bb);

		return dataPack.getDatas().size() == listSize;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		while (in.isReadable()) {
			if (!packHeaderRead) {
				if (decodePackHeader(ctx, in)) {
					packHeaderRead = true;
				}
			} else {
				if (decodePackBody(ctx, in)) {
					packHeaderRead = false; // reset state
					if (dataPack != null) {
						out.add(dataPack);
						dataPack = null;
					}
				} else {
					// no action
				}
			}
		}
	}

}
