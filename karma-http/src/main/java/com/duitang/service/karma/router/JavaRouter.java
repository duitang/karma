package com.duitang.service.karma.router;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaOverloadException;
import com.duitang.service.karma.base.MetricCenter;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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

    private String LATENCY_NAME = JavaRouter.class.getCanonicalName() + ".latency";

    public KarmaJobRunner(RPCContext ctx, BinaryPacketRaw rawPack) {
      this.ctx = ctx;
      this.raw = rawPack;
      this.submitTime = System.nanoTime();
      this.schdTime = 0;
      this.sdf = new SimpleDateFormat("HH:mm:ss.S");
    }

    @Override
    public void run() {
      BinaryPacketData data = null;
      schdTime = System.nanoTime();
      long latencyNanos = this.schdTime - this.submitTime;
      long latency = TimeUnit.NANOSECONDS.toMillis(latencyNanos);
      do {
        try {
          MetricCenter.record(LATENCY_NAME, latencyNanos);
          if (latency > 200L) {
            String info = String.format("router_latency:%d,remaining capacity:%d",
                latency, execPool.getQueue().remainingCapacity()
            );
            out.warn(info);
          }

          data = BinaryPacketHelper.fromRawToData(raw);
          if (BinaryPacketHelper.isPing(data)) {
            long g = pingCount.incrementAndGet();
            if (g % 10000 == 0) {
              out.info("channel ping checkpoint: " + g);
            }
            break;
          }

          //如果延迟超过maxQueuingLatency说明系统过载，直接报错不再执行业务逻辑
          if (latency >= maxQueuingLatency) throw new KarmaOverloadException(data.method);

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
          int tries = 0;
          Throwable root = e;
          while (root.getCause() != null && tries++ < 5) root = root.getCause();
          data.ex = root;
        }
      } while (false);

      if (raw.ctx != null) {
        raw.ctx.writeAndFlush(data.getBytes());
      }
    }
  }
}