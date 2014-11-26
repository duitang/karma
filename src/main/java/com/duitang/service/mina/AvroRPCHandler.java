package com.duitang.service.mina;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.duitang.service.base.CallbackRepository;

public class AvroRPCHandler extends IoHandlerAdapter {

	protected CallbackRepository cbcenter;

	public AvroRPCHandler(CallbackRepository cbcenter) {
		this.cbcenter = cbcenter;
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		NettyDataPack dataPack = (NettyDataPack) message;
		Callback<List<ByteBuffer>> callback = cbcenter.pop(dataPack.getSerial());
		if (callback == null) {
			throw new RuntimeException("Missing previous call info");
		}
		callback.handleResult(dataPack.getDatas());
		super.messageReceived(session, message);
	}

}
