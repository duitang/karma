package com.duitang.service.karma.router;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaOverloadException;
import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;
import com.duitang.service.karma.support.InstanceTagHolder;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TraceContextHolder;

public class JavaRouter implements Router<BinaryPacketRaw>, Closeable {

	final static Logger out = LoggerFactory.getLogger(JavaRouter.class);
	final static int CORE_SIZE = 150;

	protected RPCHandler handler;
	protected ThreadPoolExecutor execPool = new ThreadPoolExecutor(CORE_SIZE, 200, 300L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(10000));

	protected AtomicLong pingCount = new AtomicLong(0);
	protected int maxQueuingLatency = 500;

	protected String host; // for trace information
	protected int port; // for trace information

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

	@Override
	public void close() throws IOException {
		if (execPool != null) {
			execPool.shutdown();
			execPool = null;
		}
	}

	class KarmaJobRunner implements Runnable {

		RPCContext ctx;
		BinaryPacketRaw raw;
		long submitTime;
		long schdTime;
		SimpleDateFormat sdf;
		Future<?> future;
		TraceCell tc;

		public KarmaJobRunner(RPCContext ctx, BinaryPacketRaw rawPack) {
			tc = new TraceCell(false, host, port);
			tc.pid = InstanceTagHolder.INSTANCE_TAG.pid;
			this.ctx = ctx;
			this.raw = rawPack;
			this.submitTime = System.nanoTime();
			this.schdTime = 0;
			this.sdf = new SimpleDateFormat("HH:mm:ss.S");
		}

		@Override
		public void run() {
			TraceContextHolder.push(tc);
			tc.active();
			// notice: acitve here because maybe scheduled timeout
			BinaryPacketData data = null;
			schdTime = System.nanoTime();
			long latencyNanos = this.schdTime - this.submitTime;
			long latency = TimeUnit.NANOSECONDS.toMillis(latencyNanos);
			try {
				do {
					try {

						if (latency > 200L) {
							String info = String.format("router_latency:%d,remaining capacity:%d", latency,
									execPool.getQueue().remainingCapacity());
							out.warn(info);
						}

						data = BinaryPacketHelper.fromRawToData(raw);
						tc.fillInfo(data.conf, data.domain, data.method, null);

						if (BinaryPacketHelper.isPing(data)) {
							long g = pingCount.incrementAndGet();
							if (g % 10000 == 0) {
								out.info("channel ping checkpoint: " + g);
							}
							break;
						}

						// 如果延迟超过maxQueuingLatency说明系统过载，直接报错不再执行业务逻辑
						if (latency >= maxQueuingLatency)
							throw new KarmaOverloadException(data.method);

						ctx.name = data.domain;
						ctx.method = data.method;
						ctx.params = data.param;
						ctx.tc = new TraceCell(false, host, port); // no leak
						ctx.tc.setIds(tc.traceId, tc.spanId);
						ctx.tc.isLocal = true;
						ctx.tc.name = data.method;
						ctx.tc.clazzName = data.domain;
						handler.lookUp(ctx);
						handler.invoke(ctx);
						data.ret = ctx.ret;
					} catch (Throwable e) {
						if (data == null) {
							data = new BinaryPacketData();
						}
						int tries = 0;
						Throwable root = e;
						while (root.getCause() != null && tries++ < 5)
							root = root.getCause();
						data.ex = root;
					}
				} while (false);

				if (raw.ctx != null) {
					raw.ctx.writeAndFlush(data.getBytes());
				}
			} finally {
				tc.passivate(data.ex);
				TraceContextHolder.release();
				KarmaServerConfig.tracer.visit(tc);
			}
		}
	}

	@Override
	public void setHostInfo(String host, int port) {
		this.host = host;
		this.port = port;
	}
}
