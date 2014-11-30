package com.duitang.service.codecs;


public class NettyFrameEncoder { 
//extends MessageToByteEncoder<NettyDataPack> {

//	private void getPackHeader(NettyDataPack dataPack, ByteBuf out) {
//		out.writeInt(dataPack.getSerial());
//		out.writeInt(dataPack.getDatas().size());
//	}
//
//	private void getLengthHeader(ByteBuffer buf, ByteBuf out) {
//		out.writeInt(buf.limit());
//	}
//
//	@Override
//	protected void encode(ChannelHandlerContext ctx, NettyDataPack msg, ByteBuf out) throws Exception {
//		NettyDataPack dataPack = (NettyDataPack) msg;
//		List<ByteBuffer> origs = dataPack.getDatas();
//		// prepend a pack header including serial number and list size
//		getPackHeader(dataPack, out);
//		for (ByteBuffer b : origs) {
//			// for each buffer prepend length field
//			getLengthHeader(b, out);
//			out.writeBytes(b);
//		}
//	}

}
