package com.duitang.service.karma.demo;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Assert;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.client.KarmaClient;

public class CacheNettyServiceTest {

	ServerBootstrap boot = null;
	IDemoService cli = null;

	// @Before
	public void setUp() throws Exception {
		LogManager.getLogger("com.duitang.service").setLevel(Level.ALL);
		MemoryCacheService impl = new MemoryCacheService(true);
		boot = new ServerBootstrap();
		boot.addService(IDemoService.class, impl);
		boot.startUp(9090);

		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class,
				Arrays.asList("localhost:9999"), "dev1");
		cli = client.getService();

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

		}

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

		Thread.sleep(5000);
	}

	// @Test
	public void testCloseit() throws Exception {

	}

}
