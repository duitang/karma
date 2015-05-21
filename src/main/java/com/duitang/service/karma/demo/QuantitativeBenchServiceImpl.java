package com.duitang.service.karma.demo;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author kevx
 * @since 3:17:06 PM May 21, 2015
 */
public class QuantitativeBenchServiceImpl implements QuantitativeBenchService {

    private AtomicLong totalBytes = new AtomicLong(0);
    private AtomicLong totalCalls = new AtomicLong(0);
    
    private Random rand = new Random(System.currentTimeMillis());
    
    @Override
    public boolean benchWithPayload(int maxWaitMs, byte[] payload) {
        totalCalls.incrementAndGet();
        if (payload != null) totalBytes.addAndGet(payload.length);
        try {
            Thread.sleep(maxWaitMs);
        } catch (InterruptedException e) {
        }
        return true;
    }

    @Override
    public String queryCount() {
        long mb = totalBytes.get() / 1024 / 1024;
        String ret = String.format("bytes: %d MB  calls: %d\n", mb, totalCalls.get());
        totalBytes.set(0);
        totalCalls.set(0);
        return ret;
    }

}
