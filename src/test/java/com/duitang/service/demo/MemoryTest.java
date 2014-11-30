package com.duitang.service.demo;

import org.junit.Test;

import com.duitang.service.base.ClientFactory;
import com.duitang.service.mina.AvroRPCHandler;

public class MemoryTest {

	// @Test
	public void test1() {
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "localhost" + ":" + 9999);

		cli = fac.create();
		boolean r = cli.memory_setString("aaa", "bbb", 50000);
		System.out.println(r);
		String rr = cli.memory_getString("aaa");
		System.out.println(rr);
		// rr = cli.memory_getString("aaa");
		// System.out.println(rr);

		fac.release(cli);
		cli = fac.create();
		rr = cli.memory_getString("aaa");
		System.out.println(rr);
		fac.release(cli);

	}

	// @Test
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

	@Test
	public void testHuge() {
		AvroRPCHandler.debugMode = true;
		AvroRPCHandler.debugOutputCount = 1;
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "localhost" + ":" + 9999);

		int sz = 50000;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sz; i++) {
			sb.append("a");
		}

		long ts = System.currentTimeMillis();
		cli = fac.create();
		ts = System.currentTimeMillis() - ts;
		System.out.println("created ........................." + ts);
		ts = System.currentTimeMillis();
		boolean r = cli.memory_setString("aaa", sb.toString(), 50000);
		ts = System.currentTimeMillis() - ts;
		System.out.println(r);
		System.out.println("........................." + ts);
		ts = System.currentTimeMillis();
		String rr = cli.memory_getString("aaa");
		ts = System.currentTimeMillis() - ts;
		System.out.println("........................." + ts);
		System.out.println(rr);
		fac.release(cli);
	}

}
