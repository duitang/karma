package com.duitang.service.karma.router;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaOverloadException;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;
import com.duitang.service.karma.pipe.RpcStatPipe;
import com.duitang.service.karma.support.CCT;
import com.duitang.service.karma.support.TraceChainDO;

public class JavaRouter implements Router<BinaryPacketRaw> {

	final static Logger out = Logger.getLogger(JavaRouter.class);
	final static int CORE_SIZE = 150;
	
	protected RPCHandler handler;
	protected ThreadPoolExecutor execPool = new ThreadPoolExecutor(CORE_SIZE, 200, 300L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000));
	
	protected AtomicLong pingCount = new AtomicLong(0);
	protected int maxQueuingLatency = 0;
	
	@Override
	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}

	public void setMaxQueuingLatency(int maxQueuingLatency) {
        this.maxQueuingLatency = maxQueuingLatency;
    }

    @Override
	public void route(RPCContext ctx, BinaryPacketRaw raw) throws KarmaException {
        KarmaJobRunner k = new KarmaJobRunner(ctx, raw);
        Future<?> future = execPool.submit(k);
        k.future = future;
		int sz = execPool.getActiveCount();
		if (sz >= CORE_SIZE) {
		    out.warn("JavaRouter_threads_exceed:" + sz);
		}
	}

	class KarmaJobRunner implements Runnable {

		RPCContext ctx;
		BinaryPacketRaw raw;
		long submitTime;
		long schdTime;
		SimpleDateFormat sdf;
		Future<?> future;
		
		public KarmaJobRunner(RPCContext ctx, BinaryPacketRaw rawPack) {
			this.ctx = ctx;
			this.raw = rawPack;
			this.submitTime = System.currentTimeMillis();
			this.schdTime = 0;
			this.sdf = new SimpleDateFormat("HH:mm:ss.S");
		}

		@Override
		public void run() {
			BinaryPacketData data = null;
			schdTime = System.currentTimeMillis();
			long latency = this.schdTime - this.submitTime;
			do {
				try {
		            if (latency > 200L) {
		                String info = String.format("%s_JavaRouter_latency:%d,Qsize:%d", 
		                    sdf.format(new Date()), latency, execPool.getTaskCount()
		                );
		                out.warn(info);
		                RpcStatPipe.stat(RpcStatPipe.CAT_HIGH_LATENCY, latency);
		            }
		            
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
					//如果延迟超过maxQueuingLatency说明系统过载，直接报错不再执行业务逻辑
                    if (latency >= maxQueuingLatency) throw new KarmaOverloadException();
                    
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
					if (e.getCause() == null) {
					    data.ex = e;
					} else data.ex = e.getCause();
				}
			} while (false);
			
			if (raw.ctx != null) {
				raw.ctx.writeAndFlush(data.getBytes());
			}
		}
	}
}
