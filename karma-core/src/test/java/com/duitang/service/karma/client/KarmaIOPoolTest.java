package com.duitang.service.karma.client;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.demo.DemoServer;

public class KarmaIOPoolTest {

	DemoServer server;

	@Before
	public void setUp() throws Exception {
		server = new DemoServer(9999);
	}

	@After
	public void tearDown() throws Exception {
		server.shutdown();
	}

	@Test
	public void test1() throws Exception {
		KarmaIOPool pool = new KarmaIOPool();

		Set<KarmaIoSession> conns = new HashSet<KarmaIoSession>();

		for (int i = 0; i < 100; i++) {
			KarmaIoSession session = pool.getIOSession("localhost:9999");
			Assert.assertTrue(session.isAlive());
			conns.add(session);
			pool.releaseIOSession(session);
		}
		Assert.assertTrue(pool.isAlive());

		pool.resetPool();

		for (KarmaIoSession s : conns) {
			Assert.assertFalse(s.isAlive());
		}

		try {
			// not exist
			pool.getIOSession("localhost:9998");
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Assert.assertTrue(pool.ioPool.get("localhost:9998").getNumActive() < 1);
		Assert.assertTrue(pool.ioPool.get("localhost:9998").getNumIdle() < 1);

		pool.close();
		Assert.assertFalse(pool.isAlive());
	}

}
