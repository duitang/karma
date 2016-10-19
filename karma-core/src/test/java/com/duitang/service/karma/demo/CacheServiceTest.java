package com.duitang.service.karma.demo;

import org.junit.After;
import org.junit.Assert;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.base.ClientFactory;
import com.duitang.service.karma.boot.ServerBootstrap;

public class CacheServiceTest {

	ServerBootstrap boot = null;
	ClientFactory<IDemoService> fac = null;

	// @Before
	public void setUp() {
		MemoryCacheService impl = new MemoryCacheService(true);
		boot = new ServerBootstrap();
		try {
			boot.startUp(new Class[] { IDemoService.class }, new Object[] { impl }, 9090);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		fac = ClientFactory.createFactory(IDemoService.class);
		fac.setUrl("127.0.0.1:9091");
	}

	@After
	public void destroy() {
		boot.shutdown();
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
		IDemoService cli = fac.create();
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
		fac.release(cli);
		Thread.sleep(5000);
	}
}
