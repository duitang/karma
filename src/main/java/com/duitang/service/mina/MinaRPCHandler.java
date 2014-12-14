package com.duitang.service.mina;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaRPCHandler extends IoHandlerAdapter {

	static protected Logger logger = LoggerFactory.getLogger(MinaRPCHandler.class);
	static protected ExecutorService actionPool = Executors.newFixedThreadPool(100);

	protected MinaEpoll epoll;

	public MinaRPCHandler(MinaEpoll epoll) {
		this.epoll = epoll;
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		NettyDataPack dataPack = (NettyDataPack) message;
		Integer uuid = dataPack.getSerial();
		Callback<List<ByteBuffer>> callback = this.epoll.callbacks.get(uuid);
		if (callback == null) {
			throw new RuntimeException("Missing previous call info " + dataPack.getSerial());
		}
		this.epoll.callbacks.remove(uuid);
		actionPool.submit(new WrapperCallback(callback, dataPack));
		// new WrapperCallback(callback, dataPack).run();
	}

	class WrapperCallback implements Runnable {

		Callback<List<ByteBuffer>> cb;
		NettyDataPack data;

		public WrapperCallback(Callback<List<ByteBuffer>> cb, NettyDataPack data) {
			this.cb = cb;
			this.data = data;
		}

		@Override
		public void run() {
			try {
				// System.out.println("with ====> " + data.getDatas().size());
				cb.handleResult(data.getDatas());
			} catch (Exception e) {
				logger.error("WrapperCallback:", e);
			}
		}
	}

}
