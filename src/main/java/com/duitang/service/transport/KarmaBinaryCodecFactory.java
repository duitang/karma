package com.duitang.service.transport;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class KarmaBinaryCodecFactory implements ProtocolCodecFactory {

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return (ProtocolEncoder) session.getAttribute("encoder", new KarmaBinaryEncoder());
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return (ProtocolDecoder) session.getAttribute("decoder", new KarmaBinaryDecoder());
	}

}
