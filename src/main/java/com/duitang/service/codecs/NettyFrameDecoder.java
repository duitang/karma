package com.duitang.service.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;

public class NettyFrameDecoder extends ByteToMessageDecoder {
	private boolean packHeaderRead = false;
	private int listSize;
	private NettyDataPack dataPack;
	private final long maxMem;
	private static final long SIZEOF_REF = 8L; // mem usage of 64-bit pointer

	public NettyFrameDecoder() {
		maxMem = Runtime.getRuntime().maxMemory();
	}

	private boolean decodePackHeader(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		if (buffer.readableBytes() < 8) {
			return false;
		}

		int serial = buffer.readInt();
		int listSize = buffer.readInt();

		// Sanity check to reduce likelihood of invalid requests being honored.
		// Only allow 10% of available memory to go towards this list (too
		// much!)
		if (listSize * SIZEOF_REF > 0.1 * maxMem) {
			ctx.channel().close().await();
			throw new AvroRuntimeException("Excessively large list allocation " + "request detected: " + listSize
			        + " items! Connection closed.");
		}

		this.listSize = listSize;
		dataPack = new NettyDataPack(serial, new ArrayList<ByteBuffer>(listSize));

		return true;
	}

	private boolean decodePackBody(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		if (buffer.readableBytes() < 4) {
			return false;
		}

		buffer.markReaderIndex();

		int length = buffer.readInt();

		if (buffer.readableBytes() < length) {
			buffer.resetReaderIndex();
			return false;
		}

		ByteBuffer bb = ByteBuffer.allocate(length);
		buffer.readBytes(bb);
		bb.flip();
		dataPack.getDatas().add(bb);

		return dataPack.getDatas().size() == listSize;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (!packHeaderRead) {
			if (decodePackHeader(ctx, in)) {
				packHeaderRead = true;
			}
		} else {
			if (decodePackBody(ctx, in)) {
				packHeaderRead = false; // reset state
				out.add(dataPack);
			} else {
			}
		}
	}

}
