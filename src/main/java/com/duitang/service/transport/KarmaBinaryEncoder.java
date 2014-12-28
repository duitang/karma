package com.duitang.service.transport;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.duitang.service.meta.BinaryPacketData;

public class KarmaBinaryEncoder extends ProtocolEncoderAdapter {

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		BinaryPacketData packet = (BinaryPacketData) message;
//		IoBuffer buf = packet.getBytes();
//		out.write(buf);
	}

}
