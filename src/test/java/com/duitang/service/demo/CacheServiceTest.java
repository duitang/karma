package com.duitang.service.demo;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.base.PooledClient;
import com.duitang.service.base.ServerBootstrap;
import com.duitang.service.demo.MemoryCache;
import com.duitang.service.demo.MemoryCache.Iface;
import com.duitang.service.demo.MemoryCacheClientFactory;
import com.duitang.service.demo.MemoryCacheService;


public class CacheServiceTest {

	@Before
	public void setUp() {

	}

	@Test
	public void testBoot() {
		MemoryCacheService impl = new MemoryCacheService();
		TProcessor processor = new MemoryCache.Processor<MemoryCache.Iface>(impl);
		ServerBootstrap boot = new ServerBootstrap();
		try {
			boot.startUp(processor, 9090);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		MemoryCacheClientFactory fac = new MemoryCacheClientFactory();
		fac.setUrl("127.0.0.1:9090");
		PooledClient<MemoryCache.Iface> pool = new PooledClient<MemoryCache.Iface>(fac);
		Iface cli = pool.getClient();
		try {
			String key = "aaaa";
			String value = "bbbb";
			cli.setString(key, value, 1111);
			String sss = cli.getString(key);
			Assert.assertEquals(value, sss);
		} catch (TException e) {
			e.printStackTrace();
			Assert.fail();
		}
		// pool.retClient(cli);
		// pool.close();

		long stime = 10000000L;
		for (long i = 1000; i < stime; i += 1000) {
			try {
				// System.out.println(fac.isValid((Client) cli));
				Thread.sleep(1000);
				if (i > 5000) {
					boot.shutdown();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
