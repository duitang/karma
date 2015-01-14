package com.duitang.service.karma.router;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;

public class JavaRouter implements Router<BinaryPacketRaw> {

	final static Logger out = Logger.getLogger(JavaRouter.class);

	protected RPCHandler handler;
	protected ExecutorService execPool = new ThreadPoolExecutor(5, 100, 300L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000));

	protected AtomicLong pingCount = new AtomicLong(0);

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
			do {
				try {
					data = BinaryPacketHelper.fromRawToData(raw);
					if (BinaryPacketHelper.isPing(data)) {
						long g = pingCount.incrementAndGet();
						if (g % 10000 == 0) {
							out.info("channel ping checkpoint: " + g);
						}
						break;
					}
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
				}
			} while (false);
			if (raw.ctx != null) {
				raw.ctx.writeAndFlush(data.getBytes());
			}
		}
	}
}
