package com.duitang.service.karma.demo;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

    @Override
    public DemoRPCDTO unstableTimeoutMethod() {
        if (rand.nextInt(10) < 5) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        DemoRPCDTO dto = new DemoRPCDTO();
        dto.setA("asd");
        dto.setB(Lists.newArrayList(1.1f, 1.2f));
        dto.setC(Maps.<String,Double>newHashMap());
        return dto;
    }

    @Override
    public String stableMethod() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    public long unstableExceptionMethod() {
        Validate.isTrue(rand.nextInt(10) < 5);
        return System.currentTimeMillis();
    }

}
