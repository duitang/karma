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
   * @param payload   负载，模拟服务调用数据
   */
  public boolean benchWithPayload(int maxWaitMs, byte[] payload);

  /**
   * 不稳定的方法，该方法有50%的可能超时
   */
  public DemoRPCDTO unstableTimeoutMethod();

  /**
   * 不稳定的方法，该方法有50%的可能抛出异常
   */
  public long unstableExceptionMethod();

  /**
   * 稳定的方法，绝不超时
   */
  public String stableMethod();

  public String queryCount();
}
