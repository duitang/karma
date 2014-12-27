package com.duitang.service.transport;

import java.nio.ByteBuffer;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoProcessor;
import org.apache.mina.core.session.IoSession;

import com.duitang.service.KarmaException;
import com.duitang.service.handler.RPCContext;
import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.meta.BinaryPacketHelper;
import com.duitang.service.meta.BinaryPacketRaw;
import com.duitang.service.router.Router;

public class JavaServerHandler extends IoHandlerAdapter {

	protected Router<BinaryPacketRaw> router;
	protected IoProcessor proc;

	public void setRouter(Router<BinaryPacketRaw> router) {
		this.router = router;
	}

	public void setProc(IoProcessor proc) {
		this.proc = proc;
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		// FIXME
		proc.remove(session);
		super.exceptionCaught(session, cause);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		BinaryPacketRaw raw = (BinaryPacketRaw) message;
		raw.iochannel = session;
		try {
			router.route(new RPCContext(), raw);
		} catch (Throwable e) {
			raw.setError(ByteBuffer.wrap(e.getMessage().getBytes()));
			BinaryPacketData data = null;
			try {
				data = BinaryPacketHelper.fromRawToData(raw);
			} catch (KarmaException e1) {
				data = new BinaryPacketData();
				data.ex = e;
			}
			session.write(data.getBytes()); // immediately error
		}
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// FIXME
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		proc.remove(session);
		super.sessionClosed(session);
	}

}
