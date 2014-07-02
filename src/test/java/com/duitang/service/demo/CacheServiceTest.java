package com.duitang.service.demo;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.duitang.service.base.PooledClient;
import com.duitang.service.base.ServerBootstrap;

public class CacheServiceTest {

	@Before
	public void setUp() {

	}

	@Test
	public void testBoot() {
		MemoryCacheService impl = new MemoryCacheService();
		ServerBootstrap boot = new ServerBootstrap();
		try {
			boot.startUp(DemoService.class, impl, 9090);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		MemoryCacheClientFactory fac = new MemoryCacheClientFactory();
		fac.setUrl("http://127.0.0.1:9090");
		PooledClient<DemoService> pool = new PooledClient<DemoService>(fac);
		DemoService cli = pool.getClient();
		try {
			String key = "aaaa";
			String value = "bbbb";
			cli.memory_setString(key, value, 1111);
			CharSequence sss = (CharSequence) cli.memory_getString(key);
			Assert.assertEquals(value, String.valueOf(sss));
			System.out.println(sss);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
		pool.retClient(cli);
		pool.close();

		long stime = 10L;
		// long stime = 10000000L;
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
