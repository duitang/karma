package com.duitang.service.router;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.duitang.service.KarmaException;
import com.duitang.service.handler.RPCContext;
import com.duitang.service.handler.RPCHandler;
import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.meta.BinaryPacketHelper;
import com.duitang.service.meta.BinaryPacketRaw;

public class JavaRouter implements Router<BinaryPacketRaw> {

	protected RPCHandler handler;
	protected ExecutorService execPool = Executors.newCachedThreadPool();

	@Override
	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}

	@Override
	public void route(RPCContext ctx, BinaryPacketRaw raw) throws KarmaException {
		execPool.submit(new KarmaJobRunner(ctx, raw));
	}

	class KarmaJobRunner implements Runnable {

		RPCContext ctx;
		BinaryPacketRaw raw;

		public KarmaJobRunner(RPCContext ctx, BinaryPacketRaw rawPack) {
			this.ctx = ctx;
			this.raw = rawPack;
		}

		@Override
		public void run() {
			BinaryPacketData data = null;
			try {
				data = BinaryPacketHelper.fromRawToData(raw);
				ctx.name = data.domain;
				ctx.method = data.method;
				ctx.params = data.param;
				handler.lookUp(ctx);
				handler.invoke(ctx);
				data.ret = ctx.ret;
			} catch (Throwable e) {
				if (data == null) {
					data = new BinaryPacketData();
				}
				data.ex = e;
			} finally {
				if (raw.iochannel != null) {
					raw.iochannel.write(data.getBytes());
				}
			}
		}
	}
}
