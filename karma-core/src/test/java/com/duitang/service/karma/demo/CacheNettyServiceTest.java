package com.duitang.service.karma.demo;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assert;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.base.ClientFactory;
import com.duitang.service.karma.boot.ServerBootstrap;

public class CacheNettyServiceTest {

	ServerBootstrap boot = null;
	ClientFactory<IDemoService> fac = null;

	// @Before
	public void setUp() throws Exception {
		LogManager.getLogger("com.duitang.service").setLevel(Level.ALL);
		MemoryCacheService impl = new MemoryCacheService(true);
		boot = new ServerBootstrap();
		boot.addService(IDemoService.class, impl);
		boot.startUp(9090);

		fac = ClientFactory.createFactory(IDemoService.class);
		fac.setUrl("127.0.0.1:9091");

	}

	// @After
	public void destroy() {
		boot.shutdown();
	}

	// @Test
	public void testSleep() {
		try {
			Thread.sleep(20000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	public void testBoot() {
		IDemoService cli = fac.create();
		try {
			String key = "aaaa";
			String value = "bbbb";
			System.out.println(cli.memory_setString(key, value, 1111));
			CharSequence sss = (CharSequence) cli.memory_getString(key);
			Assert.assertEquals(value, String.valueOf(sss));
			System.out.println(sss);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			fac.release(cli);
		}
		cli = fac.create();
		try {
			String key = "aaaa";
			String value = "bbbb";
			System.out.println(cli.memory_setString(key, value, 1111));
			CharSequence sss = (CharSequence) cli.memory_getString(key);
			System.out.println(sss);
			Assert.assertEquals(value, String.valueOf(sss));
			System.out.println(sss);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			fac.release(cli);
		}

		long stime = 10L;
		// long stime = 10000000L;
		for (long i = 1000; i < stime; i += 1000) {
			try {
				// System.out.println(fac.isValid((Client) cli));
				Thread.sleep(1000);
				if (i > 5000) {
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// @Test
	public void testMetric() throws Exception {
		IDemoService cli = fac.create();
		for (int i = 0; i < 10; i++) {
			System.out.println("----->" + cli.trace_msg("wait_100", 100));
		}
		boot.shutdown();
		for (int i = 0; i < 5; i++) {
			try {
				System.out.println("----->" + cli.trace_msg("wait_600", 600));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		fac.release(cli);
		Thread.sleep(5000);
	}

	// @Test
	public void testCloseit() throws Exception {
		IDemoService cli = fac.create();
		fac.release(cli);
	}

}
