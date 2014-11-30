package com.duitang.service.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class AvroCodecFactory implements ProtocolCodecFactory {

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		ProtocolEncoder ret = (ProtocolEncoder) session.getAttribute("encoder");
		if (ret == null) {
			ret = new AvroEncoder();
			session.setAttributeIfAbsent("encoder", ret);
		}
		return ret;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		ProtocolDecoder ret = (ProtocolDecoder) session.getAttribute("decoder");
		if (ret == null) {
			ret = new AvroDecoder();
			session.setAttributeIfAbsent("decoder", ret);
		}
		return ret;
	}

}
