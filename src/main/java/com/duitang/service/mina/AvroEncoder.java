package com.duitang.service.mina;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class AvroEncoder extends ProtocolEncoderAdapter {

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		NettyDataPack msg = (NettyDataPack) message;
		List<ByteBuffer> origs = msg.getDatas();
		// prepend a pack header including serial number and list size
		IoBuffer data = null;
		data = getPackHeader(msg);
		out.write(data);
		for (ByteBuffer b : origs) {
			// for each buffer prepend length field
			data = getLengthHeader(b);
			out.write(data);
			data = IoBuffer.wrap(b.array(), b.position(), b.remaining());
			out.write(data);
		}
	}

	private IoBuffer getPackHeader(NettyDataPack dataPack) {
		IoBuffer ret = IoBuffer.allocate(8);
		ret.putInt(dataPack.getSerial());
		ret.putInt(dataPack.getDatas().size());
		return ret.flip();
	}

	private IoBuffer getLengthHeader(ByteBuffer buf) {
		IoBuffer ret = IoBuffer.allocate(4);
		ret.putInt(buf.limit());
		return ret.flip();
	}

}
