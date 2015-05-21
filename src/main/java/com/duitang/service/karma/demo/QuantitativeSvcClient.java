package com.duitang.service.karma.demo;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.math.NumberUtils;

import com.duitang.service.karma.KarmaOverloadException;
import com.duitang.service.karma.base.ClientFactory;
import com.google.common.base.Stopwatch;

/**
 * 
 * @author kevx
 * @since 3:31:12 PM May 21, 2015
 */
public class QuantitativeSvcClient {

    private static Random rand = new Random();
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        final String url = args[0].trim();
        final int nThreads = NumberUtils.toInt(args[1]);
        final long totalcall = NumberUtils.toLong(args[2]);
        final int maxwait = NumberUtils.toInt(args[3]);
        final int payloadSize = NumberUtils.toInt(args[4]);
        
        final byte[] payload = new byte[payloadSize];
        Arrays.fill(payload, Integer.valueOf(rand.nextInt()).byteValue());
        
        Class<Object> interfaceCls = (Class<Object>)Class.forName(
            "com.duitang.service.karma.demo.QuantitativeBenchService"
        );
        final ClientFactory<Object> cf0 = ClientFactory.createFactory(interfaceCls);
        cf0.setGroup("bench");
        cf0.setUrl(url);
        cf0.setTimeout(500);
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
                        e.printStackTrace();
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
        Runtime.getRuntime().exit(0);
    }

}
