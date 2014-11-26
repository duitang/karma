package com.duitang.service.demo;

import org.junit.Test;

import com.duitang.service.base.ClientFactory;

public class MemoryTest {

//	@Test
	public void test1() {
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "localhost" + ":" + 9999);

		cli = fac.create();
		boolean r = cli.memory_setString("aaa", "bbb", 50000);
		System.out.println(r);
		String rr = cli.memory_getString("aaa");
		System.out.println(rr);
		rr = cli.memory_getString("aaa");
		System.out.println(rr);

		fac.release(cli);
		cli = fac.create();
		rr = cli.memory_getString("aaa");
		System.out.println(rr);
		fac.release(cli);

	}

	 @Test
	public void testConnections() {
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "localhost" + ":" + 9999);

		boolean r = false;
		String s = null;
		int loop = 100000;
		for (int i = 0; i < loop; i++) {
			cli = fac.create();
			r = cli.memory_setString("aaa", "bbb", 5000);
			s = cli.memory_getString("aaa");
			System.out.println("[" + i + "]," + r + "," + s);
			fac.release(cli);
		}

		try {
			Thread.sleep(5000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void testHuge() {
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "localhost" + ":" + 9999);

		int sz = 50000;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sz; i++) {
			sb.append("a");
		}

		cli = fac.create();
		boolean r = cli.memory_setString("aaa", sb.toString(), 50000);
		System.out.println(r);
		String rr = cli.memory_getString("aaa");
		System.out.println(rr);
		fac.release(cli);
	}

}
