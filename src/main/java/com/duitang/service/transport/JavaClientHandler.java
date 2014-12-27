package com.duitang.service.transport;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.IoSession;

import com.duitang.service.client.KarmaClient;
import com.duitang.service.client.KarmaRemoteLatch;
import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.meta.BinaryPacketHelper;
import com.duitang.service.meta.BinaryPacketRaw;

public class JavaClientHandler extends IoHandlerAdapter {

	protected IoProcessor proc;

	public JavaClientHandler(IoProcessor proc) {
		this.proc = proc;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		// FIXME
		proc.remove(session);
		KarmaClient cli = (KarmaClient) session.getAttribute(KarmaClient.CLINET_ATTR_NAME);
		if (cli != null) {
			cli.close();
		}
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

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		proc.remove(session);
		KarmaClient cli = (KarmaClient) session.getAttribute(KarmaClient.CLINET_ATTR_NAME);
		if (cli != null) {
			cli.close();
		}
		super.sessionClosed(session);
	}

}
