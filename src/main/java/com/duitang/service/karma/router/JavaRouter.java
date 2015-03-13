package com.duitang.service.karma.router;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
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
import com.duitang.service.karma.support.CCT;
import com.duitang.service.karma.support.TraceChainDO;

public class JavaRouter implements Router<BinaryPacketRaw> {

	final static Logger out = Logger.getLogger(JavaRouter.class);

	protected RPCHandler handler;
	protected ThreadPoolExecutor execPool = new ThreadPoolExecutor(5, 100, 300L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000));

	protected AtomicLong pingCount = new AtomicLong(0);

	@Override
	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}

	@Override
	public void route(RPCContext ctx, BinaryPacketRaw raw) throws KarmaException {
		execPool.submit(new KarmaJobRunner(ctx, raw));
		int sz = execPool.getActiveCount();
		if (sz >= 99) {
		    out.warn("JavaRouter_threads_exceed:" + sz);
		}
	}

	class KarmaJobRunner implements Runnable {

		RPCContext ctx;
		BinaryPacketRaw raw;
		long submitTime;
		long schdTime;
		
		public KarmaJobRunner(RPCContext ctx, BinaryPacketRaw rawPack) {
			this.ctx = ctx;
			this.raw = rawPack;
			this.submitTime = System.currentTimeMillis();
			this.schdTime = 0;
		}

		@Override
		public void run() {
			BinaryPacketData data = null;
			schdTime = System.currentTimeMillis();
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
					if (data.conf != null && data.conf.isValid()) {
    					TraceChainDO chain = (TraceChainDO) data.conf.getConf(CCT.RPC_CONF_KEY);
    					if (chain != null) {
    					    chain.reset();
    					    long currtime = System.currentTimeMillis();
    					    long timebase = currtime;
    					    Serializable obj = data.conf.getConf("timebase");
    					    if (obj != null && obj instanceof Long) {
    					        timebase = (long) obj;
    					        chain.setTimedelta(timebase - currtime);
    					    }
    					    CCT.setForcibly(chain);
    					}
					}
					ctx.name = data.domain;
					ctx.method = data.method;
					ctx.params = data.param;
					handler.lookUp(ctx);
					try {
					    CCT.call(data.domain + "::" + data.method, false);
					    handler.invoke(ctx);
					} finally {
					    CCT.ret();
					}
					data.ret = ctx.ret;
				} catch (Throwable e) {
					if (data == null) {
						data = new BinaryPacketData();
					}
					data.ex = e;
				}
			} while (false);
			long latency = this.schdTime - this.submitTime;
			if (latency > 100L) {
			    out.warn("JavaRouter_latency:" + latency);
			}
			if (raw.ctx != null) {
				raw.ctx.writeAndFlush(data.getBytes());
			}
		}
	}
}
