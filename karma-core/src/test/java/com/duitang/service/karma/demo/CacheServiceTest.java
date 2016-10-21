package com.duitang.service.karma.demo;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.client.KarmaClient;

public class CacheServiceTest {

	ServerBootstrap boot = null;
	IDemoService cli = null;

	// @Before
	public void setUp() throws KarmaException {
		MemoryCacheService impl = new MemoryCacheService(true);
		boot = new ServerBootstrap();
		try {
			boot.startUp(new Class[] { IDemoService.class }, new Object[] { impl }, 9090);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class,
				Arrays.asList("localhost:9999"));
		cli = client.getService();
	}

	@After
	public void destroy() {
		boot.shutdown();
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
			//
		}

		// long stime = 10L;
		// // long stime = 10000000L;
		// for (long i = 1000; i < stime; i += 1000) {
		// try {
		// // System.out.println(fac.isValid((Client) cli));
		// Thread.sleep(1000);
		// if (i > 2000) {
		// break;
		// }
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
	}

	// @Test
	public void testMetric() throws Exception {
		for (int i = 0; i < 10; i++) {
			System.out.println("----->" + cli.trace_msg("wait_100", 100));
		}
		for (int i = 0; i < 5; i++) {
			try {
				System.out.println("----->" + cli.trace_msg("wait_600", 600));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Thread.sleep(5000);
	}
}
