package com.duitang.service.codecs;


public class NettyFrameDecoder {// extends ByteToMessageDecoder {

	// private final long maxMem;
	// private static final long SIZEOF_REF = 8L; // mem usage of 64-bit pointer
	// protected static AttributeKey<DecoderContext> key =
	// AttributeKey.valueOf("decoderContext");
	//
	// public NettyFrameDecoder() {
	// maxMem = Runtime.getRuntime().maxMemory();
	// }
	//
	// private boolean decodePackHeader(DecoderContext dctx, ByteBuf buffer,
	// Channel channel) throws Exception {
	// if (buffer.readableBytes() < 8) {
	// return false;
	// }
	//
	// int serial = buffer.readInt();
	// int listSize = buffer.readInt();
	//
	// // Sanity check to reduce likelihood of invalid requests being honored.
	// // Only allow 10% of available memory to go towards this list (too
	// // much!)
	// if (listSize * SIZEOF_REF > 0.1 * maxMem) {
	// channel.close().await();
	// throw new AvroRuntimeException("Excessively large list allocation " +
	// "request detected: " + listSize
	// + " items! Connection closed.");
	// }
	//
	// dctx.listSize = listSize;
	// dctx.dataPack = new NettyDataPack(serial, new
	// ArrayList<ByteBuffer>(listSize));
	//
	// return true;
	// }
	//
	// private boolean decodePackBody(DecoderContext dctx, ByteBuf buffer)
	// throws Exception {
	// if (buffer.readableBytes() < 4) {
	// return false;
	// }
	//
	// buffer.markReaderIndex();
	//
	// int length = buffer.readInt();
	//
	// if (buffer.readableBytes() < length) {
	// buffer.resetReaderIndex();
	// return false;
	// }
	//
	// ByteBuffer bb = ByteBuffer.allocate(length);
	// buffer.readBytes(bb);
	// bb.flip();
	// dctx.dataPack.getDatas().add(bb);
	//
	// return dctx.dataPack.getDatas().size() == dctx.listSize;
	// }
	//
	// @Override
	// protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object>
	// out) throws Exception {
	// Attribute<DecoderContext> attr = ctx.channel().attr(key);
	// DecoderContext dctx = attr.get();
	// if (dctx == null) {
	// dctx = new DecoderContext();
	// attr.set(dctx);
	// }
	// if (!dctx.packHeaderRead) {
	// if (decodePackHeader(dctx, in, ctx.channel())) {
	// dctx.packHeaderRead = true;
	// }
	// } else {
	// if (decodePackBody(dctx, in)) {
	// dctx.packHeaderRead = false; // reset state
	// out.add(dctx.dataPack);
	// } else {
	// }
	// }
	// }
	//
	// }
	//
	// class DecoderContext {
	// boolean packHeaderRead = false;
	// int listSize;
	// NettyDataPack dataPack;
	// }
}