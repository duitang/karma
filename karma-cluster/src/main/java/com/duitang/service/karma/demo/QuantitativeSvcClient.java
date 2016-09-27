package com.duitang.service.karma.demo;

import com.duitang.service.karma.KarmaOverloadException;
import com.duitang.service.karma.base.ClientFactory;
import com.duitang.service.karma.base.MetricCenter;

import com.google.common.base.Stopwatch;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * java -cp target/karma-test.jar com.duitang.service.karma.demo.QuantitativeSvcStarter java -cp
 * target/karma-test.jar com.duitang.service.karma.demo.QuantitativeSvcClient args: <url>
 * <n_threads> <n_calls> <maxwait> <payload_size>
 *
 * @author kevx
 * @since 3:31:12 PM May 21, 2015
 */
public class QuantitativeSvcClient {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    final String url = args[0].trim();
    final int nThreads = NumberUtils.toInt(args[1]);
    final long totalcall = NumberUtils.toLong(args[2]);
    final int maxwait = NumberUtils.toInt(args[3]);
    final int payloadSize = NumberUtils.toInt(args[4]);

    final byte[] payload = new byte[payloadSize];
    Arrays.fill(payload, Integer.valueOf(ThreadLocalRandom.current().nextInt()).byteValue());

    Class<Object> interfaceCls = (Class<Object>) Class.forName(
        "com.duitang.service.karma.demo.QuantitativeBenchService"
    );
    final ClientFactory<Object> cf0 = ClientFactory.createFactory(interfaceCls);
    cf0.setGroup("bench");
    cf0.setUrl(url);
    cf0.setTimeout(1000);
    cf0.reset();

    final AtomicLong overload = new AtomicLong();
    final AtomicLong err = new AtomicLong();
    ExecutorService exec = Executors.newFixedThreadPool(nThreads);
    Stopwatch sw = Stopwatch.createStarted();
    for (long i = 0; i < totalcall; i++) {
      exec.submit(new Runnable() {
        @Override
        public void run() {
          try {
            QuantitativeBenchService svc = (QuantitativeBenchService) cf0.create();
            svc.benchWithPayload(maxwait, payload);
            cf0.release(svc);
          } catch (KarmaOverloadException ke) {
            overload.incrementAndGet();
          } catch (Exception e) {
            e.getCause().printStackTrace();
            err.incrementAndGet();
          }
        }
      });
    }
    exec.shutdown();
    System.out.print("waiting for completion...");
    exec.awaitTermination(10, TimeUnit.MINUTES);
    sw.stop();
    System.out.println("done");
    System.out.println("elapsed: " + sw.elapsed(TimeUnit.SECONDS));
    System.out.println(String.format("overload: %d  err: %d", overload.get(), err.get()));

    Thread.sleep(3000);
    QuantitativeBenchService svc = (QuantitativeBenchService) cf0.create();
    System.out.println(svc.queryCount());

    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>");
    List<Map> samples = MetricCenter.sample();
    for (Map sample : samples) {
      System.out.println(sample);
    }
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>");

    Runtime.getRuntime().exit(0);
  }

}
