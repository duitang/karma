package com.duitang.service.testing;

import org.junit.Test;

import com.duitang.service.KarmaRuntimeException;
import com.duitang.service.base.ClientFactory;
import com.duitang.service.base.LifeCycle;
import com.duitang.service.base.ServerBootstrap;
import com.duitang.service.demo.DemoService;
import com.duitang.service.demo.MemoryCacheService;

public class StableTest {

	/**
	 * remote exception behavier
	 */
	// @Test
	public void test1() {
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("localhost:9999");
		DemoService cli = fac.create();
		long ts = System.currentTimeMillis();
		try {
			ts = System.currentTimeMillis();
			cli.getError();
		} catch (Exception e) {
			System.out.println(e.getClass().getName());
			e.printStackTrace();
		} finally {
			ts = System.currentTimeMillis() - ts;
		}
		System.out.println("time elapsed: " + ts + "ms");
		ts = System.currentTimeMillis();
		cli.getError();
		System.out.println("time elapsed: " + ts + "ms");
		System.out.println(cli.memory_setString("aaa", "bbb", 5000));
		System.out.println(cli.memory_getString("aaa"));
		fac.release(cli);
	}

	/**
	 * @throws Exception
	 * 
	 */
	@Test
	public void test2() throws Exception {
		ServerBootstrap server = new ServerBootstrap();
		server.addService(DemoService.class, new MemoryCacheService());
		server.startUp(9998);

		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("localhost:9999");
		DemoService cli = fac.create();
		System.out.println(cli.memory_setString("aaa", "bbb", 10000));
		System.out.println(cli.memory_getString("aaa"));

		server.shutdown();
		Thread.sleep(500);

		long ts = System.currentTimeMillis();
		System.out.println(((LifeCycle) cli).isAlive());
		ts = System.currentTimeMillis() - ts;
		System.out.println("alive: " + ts + "ms");

		try {
			System.out.println(cli.memory_getString("aaa"));
		} catch (KarmaRuntimeException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("....");
			e.printStackTrace();
		}

		fac.release(cli);

	}

}
