package com.duitang.service.karma.testing;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

/**
 * 
 * @author kevx
 * @since 6:07:45 PM Mar 13, 2015
 */
public class ThreadPoolTest {

    private final int CONCURR = 50;
    private final int REQCNT = 5000;
    private final int AVG_EXE_T = 10;
    private final int CORE_POOL = 100;//(int) (CONCURR * Math.sqrt(AVG_EXE_T));
            
    protected final ExecutorService pool1 = new ThreadPoolExecutor(CORE_POOL, 250, 300L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000));

    private Random rand  = new Random();
    
    private final CountDownLatch cdl = new CountDownLatch(CONCURR + 1);
    
    private final AtomicInteger errcnt = new AtomicInteger();
    
    private final Map<Long, AtomicInteger> rt = Maps.newHashMap();
    
    @Test
    public void test() throws Exception {
        for (long i = 0; i < 50; i++) rt.put(i, new AtomicInteger());
                
        for (int i = 0; i < CONCURR; i++) {
            new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < REQCNT; i++) {
                        try {
                            Thread.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        submit();
                    }
                    cdl.countDown();
                }
            }.start();
        }
        final Stopwatch sw = Stopwatch.createStarted();
        new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        Thread.sleep(1000);
                        //System.out.println(pool2.getActiveCount());
                        if (cdl.getCount() == 1) {
                            pool1.shutdown();
                            pool1.awaitTermination(10000, TimeUnit.SECONDS);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sw.stop();
                cdl.countDown();
            }
        }.start();
        cdl.await();
        Thread.sleep(500);
        System.out.println("err:" + errcnt.get());
        System.out.println("qps:" + (double)(CONCURR * REQCNT) / (double)(sw.elapsed(TimeUnit.SECONDS)));
        for (long i = 0; i < 50; i++) {
            int n = rt.get(i).get();
            if (n == 0) continue;
            System.out.println(String.format(
                "%d~%d\t%d", 
                i * 100, 
                i * 100 + 99, 
                (int)(((double)n / (double)(REQCNT * CONCURR)) * 100.0))
            );
        }
            
    }
    
    public void submit() {
        try {
            pool1.submit(new Task());
        } catch (Exception e) {
            errcnt.incrementAndGet();
        }
    }
    
    class Task implements Runnable {
        
        long t1 = 0;
        long t2 = 0;
        Task() {
            t1 = System.currentTimeMillis();
        }
        
        @Override
        public void run() {
            t2 = System.currentTimeMillis();
            try {
                Thread.sleep(rand.nextInt(AVG_EXE_T * 2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long tx = t2 - t1;
            long sec = (long) (tx / 100.0);
            rt.get(sec).incrementAndGet();
        }
    }
}
