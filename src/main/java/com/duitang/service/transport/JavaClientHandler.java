package com.duitang.service.transport;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.duitang.service.client.KarmaRemoteLatch;
import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.meta.BinaryPacketHelper;
import com.duitang.service.meta.BinaryPacketRaw;

public class JavaClientHandler extends IoHandlerAdapter {

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		// FIXME
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		BinaryPacketRaw raw = (BinaryPacketRaw) message;
		BinaryPacketData data = null;
		data = BinaryPacketHelper.fromRawToData(raw);
		KarmaRemoteLatch latch = (KarmaRemoteLatch) session.getAttribute(KarmaRemoteLatch.LATCH_NAME);
		if (data.ex != null) {
			latch.offerError(data.ex);
		} else {
			latch.offerResult(data.ret);
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// FIXME
	}

}
