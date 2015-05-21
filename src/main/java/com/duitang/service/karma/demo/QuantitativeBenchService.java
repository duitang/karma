package com.duitang.service.karma.demo;

/**
 * 量化压测服务
 * 
 * @author kevx
 * @since 3:14:46 PM May 21, 2015
 */
public interface QuantitativeBenchService {

    /**
     * @param maxWaitMs 最大等待毫秒数，模拟服务调用耗时
     * @param payload 负载，模拟服务调用数据
     */
    public boolean benchWithPayload(int maxWaitMs, byte[] payload);
    
    public String queryCount();
}
