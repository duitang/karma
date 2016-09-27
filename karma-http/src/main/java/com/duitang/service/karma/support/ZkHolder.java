package com.duitang.service.karma.support;

import com.google.common.collect.Sets;

import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author kevx
 * @since 12:22:55 PM Jun 25, 2015
 */
public class ZkHolder {

  private final static Logger log = Logger.getLogger(ZkHolder.class);

  private static CountDownLatch latch = null;

  private static ZooKeeper zk;
  private static final String connString = "std-1.zk.infra.duitang.net:3881,std-2.zk.infra.duitang.net:3881,std-3.zk.infra.duitang.net:3881";
  private static final String connStringDev = "10.1.4.11:3881";

  private static final Set<Runnable> callbacks = Sets.newHashSet();

  static {
    new Thread("ZkHolder") {
      @Override
      public void run() {
        int tries = 0;
        String cs = connString;
        if (isDev()) cs = connStringDev;
        while (true) {
          try {
            if (zk == null || !zk.getState().isAlive()) {
              latch = new CountDownLatch(1);
              if (zk != null) {
                zk.close();
              }
              zk = new ZooKeeper(cs, 3000, null);
              while (zk.getState() != States.CONNECTED && tries < 100) {
                //waiting for zk initialization
                tries++;
                Thread.sleep(500);
              }
              latch.countDown();
              for (Runnable r : callbacks) r.run();
            }
            Thread.sleep(1000);
          } catch (InterruptedException ie) {
            throw new RuntimeException("ZkHolder_halted");
          } catch (Exception e) {
            log.error("ZkHolder_failed:", e);
          }
        }
      }
    }.start();

    new Thread("ZkCallbackRunner") {
      @Override
      public void run() {
        while (true) {
          boolean b = false;
          try {
            while (latch == null) Thread.sleep(50);
            b = latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(5000);
          } catch (InterruptedException e) {
            throw new RuntimeException();
          }

          if (!b) continue;
          for (Runnable r : callbacks) {
            try {
              r.run();
            } catch (Exception e) {
              log.error("ZkCallbackRunner_failed:", e);
            }
          }
        }
      }
    }.start();
  }

  private static boolean isDev() {
    try {
      InetAddress ia = InetAddress.getLocalHost();
      if (IpRanges.isProduction(ia.getHostAddress())) {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  public static void addCallback(Runnable r, boolean immediatllyRun) {
    if (r != null) {
      callbacks.add(r);
      if (immediatllyRun) r.run();
    }
  }

  public static ZooKeeper get() {
    try {
      while (latch == null) Thread.sleep(50);
      if (latch.await(10, TimeUnit.SECONDS)) {
        return zk;
      }
    } catch (InterruptedException e) {
      log.error("", e);
    }
    return null;
  }

}
