package com.duitang.service.karma.testing;

import com.duitang.service.karma.client.WRRBalancer;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BalancingTest {

  final Random rand = new Random();
  List<String> servers = Lists.newArrayList("s0", "s1", "s2", "s3", "s4", "s5");
  List<String> newServers = Lists.newArrayList("s1", "s2", "s3", "s4", "s6", "s7");

  final WRRBalancer wrr = WRRBalancer.getInstance("mandala", servers);
  final Map<String, AtomicInteger> counts = Maps.newHashMap();
  final Map<String, AtomicInteger> errcounts = Maps.newHashMap();

  private void safeSleep(int n) {
    try {
      Thread.sleep(n);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private Runnable make(final String s) {
    return new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < 5000; i++) {
          String svr = wrr.next(null);
          if (svr == null) {
            System.out.println("shit");
          } else {
            counts.get(svr).incrementAndGet();
            if (svr.equalsIgnoreCase(s) && rand.nextInt(3) == 1) {
              safeSleep(rand.nextInt(500));
              errcounts.get(svr).incrementAndGet();
              wrr.fail(svr);
            }
          }
          safeSleep(rand.nextInt(10));
        }
      }
    };
  }

  @Test
  public void test_wrrbalancer() throws Exception {
    for (String s : servers) {
      counts.put(s, new AtomicInteger(0));
      errcounts.put(s, new AtomicInteger(0));
    }
    for (String s : newServers) {
      counts.put(s, new AtomicInteger(0));
      errcounts.put(s, new AtomicInteger(0));
    }

    ExecutorService es = Executors.newFixedThreadPool(10);
    es.submit(make("s2"));
    es.submit(make("s2"));
    es.submit(make("s2"));
    es.submit(make("s3"));
    es.submit(make("s3"));
    es.submit(make("s3"));
    es.submit(make("s4"));
    es.submit(make("s4"));
    es.submit(make("s4"));
    Stopwatch sw = Stopwatch.createStarted();
    es.shutdown();
    safeSleep(4000);
    wrr.reload(newServers);
    es.awaitTermination(5, TimeUnit.MINUTES);
    int sum = 0;
    for (Map.Entry<String, AtomicInteger> e : counts.entrySet()) {
      System.out.println(e.getKey() + ":" + e.getValue().get());
      sum += e.getValue().get();
    }

    for (Map.Entry<String, AtomicInteger> e : errcounts.entrySet()) {
      System.out.println("err_" + e.getKey() + ":" + e.getValue().get());
    }
    System.out.println("sum:" + sum);
    sw.stop();
  }

}
