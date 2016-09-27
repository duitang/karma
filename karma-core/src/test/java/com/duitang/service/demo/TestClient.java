package com.duitang.service.demo;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import com.duitang.service.karma.base.ClientFactory;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.support.NameUtil;

import junit.framework.Assert;

public class TestClient {

  // @Test
  public void test0() {
    String name = NameUtil.genClientIdFromCode();
    System.out.println(name);
    Assert.assertNotSame("", name);
  }

  // @Test
  public void test1() throws Exception {
    Logger root = Logger.getRootLogger();
    String pattern = "%d %m";
    ConsoleAppender appender = new ConsoleAppender();
    appender.setLayout(new PatternLayout(pattern));
    appender.setThreshold(Level.INFO);
    appender.activateOptions();
    root.addAppender(appender);

    ServerBootstrap boot = new ServerBootstrap();
    boot.addService(IDemoService.class, new MemoryCacheService());
    boot.startUp(9999);

    Thread.sleep(100000);
  }

  @Test
  public void test2() throws Exception {
    ClientFactory<IDemoService> fac = ClientFactory.createFactory(IDemoService.class);
    fac.setUrl("localhost:9999");
    IDemoService client = fac.create();
    for (int i = 0; i < 1000; i++) {
      client.memory_setString("aaa", "bbb", 5000);
      client.memory_getString("aaa");
    }
    fac.release(client);
    Thread.sleep(20000);
  }

  public static void main(String[] args) {
    TestClient cli = new TestClient();
    cli.test0();
  }

}
