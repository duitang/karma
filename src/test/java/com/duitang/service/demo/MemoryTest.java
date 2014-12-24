package com.duitang.service.demo;

import junit.framework.Assert;

import org.junit.Test;

import com.duitang.service.base.ClientFactory;
import com.duitang.service.client.KarmaClient;
import com.duitang.service.client.KarmaIoSession;

public class MemoryTest {

	// @Test
	public void test0() throws Exception {
		KarmaIoSession session = new KarmaIoSession("localhost:9999", 500);
		DemoService cli = KarmaClient.createKarmaClient(DemoService.class, session);
		long ts = System.currentTimeMillis();
		String msg = cli.trace_msg("this will timeout ", 1000);
		ts = System.currentTimeMillis() - ts;
		System.out.println("should null ---> " + msg);
		System.out.println("time elapsed: " + ts + " ms");
	}

//	@Test
	public void test1() {
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "gpu0" + ":" + 9999);

		System.out.println("-------------------");
		cli = fac.create();
		System.out.println("...................");
		boolean r = cli.memory_setString("aaa", "bbb", 50000);
		System.out.println(r);
		System.out.println("...................");
		String rr = cli.memory_getString("aaa");
		System.out.println(rr);
		Assert.assertEquals(rr, "bbb");
		System.out.println("...................");
		rr = cli.memory_getString("aaa");
		System.out.println(rr);
		Assert.assertEquals(rr, "bbb");
		// rr = cli.memory_getString("aaa");
		// System.out.println(rr);

		fac.release(cli);
		System.out.println("-------------------");
		cli = fac.create();
		System.out.println("...................");
		rr = cli.memory_getString("aaa");
		System.out.println(rr);
		fac.release(cli);

		System.out.println("------------------- break it!");
		cli = fac.create();
		System.out.println("...................");
		long ts = System.currentTimeMillis();
		rr = cli.trace_msg("this will timeout ", 1000);
		System.out.println("time elapsed:" + (System.currentTimeMillis() - ts));
		System.out.println("should be null! ---> " + rr);
		fac.release(cli);

		System.out.println("------------------- recover!");
		cli = fac.create();
		System.out.println("...................");
		ts = System.currentTimeMillis();
		rr = cli.trace_msg("this not timeout ", 300);
		System.out.println("time elapsed:" + (System.currentTimeMillis() - ts));
		System.out.println("not null! ---> " + rr);
		fac.release(cli);

	}

	// @Test
	public void testConnections() {
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "localhost" + ":" + 9999);

		boolean r = false;
		String s = null;
		int loop = 10;
		for (int i = 0; i < loop; i++) {
			cli = fac.create();
			// r = cli.memory_setString("aaa", "bbb", 5000);
			// s = cli.memory_getString("aaa");
			long ts = System.currentTimeMillis();
			s = cli.trace_msg("+" + String.valueOf(ts) + "+", 1);
			System.out.println(System.currentTimeMillis() - ts);
			// System.out.println("[" + i + "]," + r + "," + s);
			System.out.println("[" + i + "]," + s);
			fac.release(cli);
			System.out.println("##########################");
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

		int loop = 1000;
		for (int ii = 0; ii < loop; ii++) {
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
	
	@Test
	public void testSomeError(){
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("localhost:9999");
		fac.setTimeout(100000);
		DemoService cli = fac.create();
		cli.getError();
		fac.release(cli);
	}

}
