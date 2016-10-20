package com.duitang.service.karma.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.KarmaClient;

public class MemoryTest {

	// @Test
	public void test0() throws Exception {
		List<String> urls = new ArrayList<String>();
		urls.add("localhost:9999");
		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class, urls, "dev1");
		IDemoService cli = client.getService();
		long ts = System.currentTimeMillis();
		String msg = cli.trace_msg("this will timeout ", 1000);
		ts = System.currentTimeMillis() - ts;
		System.out.println("should null ---> " + msg);
		System.out.println("time elapsed: " + ts + " ms");
	}

	// @Test
	public void test1() throws KarmaException {
		IDemoService cli = null;
		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class,
				Arrays.asList("localhost:9999"), "dev1");

		System.out.println("-------------------");
		cli = client.getService();
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

		System.out.println("-------------------");

		System.out.println("...................");
		rr = cli.memory_getString("aaa");
		System.out.println(rr);

		System.out.println("------------------- break it!");

		System.out.println("...................");
		long ts = System.currentTimeMillis();
		rr = cli.trace_msg("this will timeout ", 1000);
		System.out.println("time elapsed:" + (System.currentTimeMillis() - ts));
		System.out.println("should be null! ---> " + rr);

		System.out.println("------------------- recover!");

		System.out.println("...................");
		ts = System.currentTimeMillis();
		rr = cli.trace_msg("this not timeout ", 300);
		System.out.println("time elapsed:" + (System.currentTimeMillis() - ts));
		System.out.println("not null! ---> " + rr);

	}

	// @Test
	public void testConnections() throws KarmaException {
		IDemoService cli = null;
		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class,
				Arrays.asList("localhost:9999"), "dev1");

		// boolean r = false;
		String s = null;
		int loop = 10;
		for (int i = 0; i < loop; i++) {
			cli = client.getService();
			// r = cli.memory_setString("aaa", "bbb", 5000);
			// s = cli.memory_getString("aaa");
			long ts = System.currentTimeMillis();
			s = cli.trace_msg("+" + String.valueOf(ts) + "+", 1);
			System.out.println(System.currentTimeMillis() - ts);
			// System.out.println("[" + i + "]," + r + "," + s);
			System.out.println("[" + i + "]," + s);

			System.out.println("##########################");
		}

		try {
			Thread.sleep(5000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void testHuge() throws KarmaException {
		IDemoService cli = null;
		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class,
				Arrays.asList("localhost:9999"), "dev1");

		int sz = 50000;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sz; i++) {
			sb.append("a");
		}

		int loop = 1000;
		for (int ii = 0; ii < loop; ii++) {
			long ts = System.currentTimeMillis();
			cli = client.getService();
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

		}
	}

	// @Test
	public void testSomeError() throws KarmaException {
		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class,
				Arrays.asList("localhost:9999"), "dev1");
		IDemoService cli = client.getService();
		cli.getError();

	}

}
