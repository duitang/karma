package com.duitang.service.base;

import io.airlift.units.Duration;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.thrift.protocol.TProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PooledClientTest {

	public static Set<DummyClient> all = new HashSet<DummyClient>();
	protected DummyClientFactory fac;
	protected PooledClient<DummyClient> pool;
	protected ServerSocket folk;
	protected int capacity;
	protected int idle;

	@Before
	public void setUp() {
		all.clear();
		capacity = 50;
		idle = capacity;
		fac = new DummyClientFactory();
		fac.setUrl("localhost:8080");
		pool = new PooledClient<DummyClient>(fac, capacity, idle);
		try {
			folk = new ServerSocket(8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void clearUp() {
		try {
			folk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void testSimpleGetRet() {
		Set<DummyClient> checker = new HashSet<DummyClient>();
		int sz = capacity;
		DummyClient cli;
		for (int i = 0; i < sz; i++) {
			cli = pool.getClient();
			Assert.assertNotNull(cli);
			Assert.assertFalse(checker.contains(cli)); // not in used
			checker.add(cli);
		}
		for (DummyClient cl : checker) {
			pool.retClient(cl);
		}
		for (int i = 0; i < sz; i++) {
			cli = pool.getClient();
			Assert.assertNotNull(cli);
			Assert.assertTrue(checker.contains(cli)); // already allocated
		}
		for (DummyClient cl : checker) {
			pool.retClient(cl);
		}
	}

	// @Test
	public void testNoRelease() {
		all.clear();
		final int total_connection = 30;
		fac.INST_CONNECT_TIMEOUT = new Duration(1, TimeUnit.SECONDS);
		final PooledClient<DummyClient> thepool = new PooledClient<DummyClient>(fac);
		Thread[] workers = new Thread[total_connection + 10];
		final CountDownLatch latch = new CountDownLatch(workers.length);
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						DummyClient cli = null;
						for (int i = 0; i < 1000; i++) {
							cli = thepool.getClient();
							Assert.assertNotNull(cli);
							thepool.retClient(cli);
						}
					} finally {
						latch.countDown();
					}
				}

			});
			workers[i].start();
		}
		try {
			latch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		System.out.println("about to shutdown the pool");
		thepool.close(); // ensure no leak after pool shutdown
		System.out.println("Total created object = " + all.size());
		Assert.assertTrue(all.size() > 0);

		// ensure client is not created every time
		// Assert.assertTrue(all.size() < workers.length);
		// Assert.assertTrue(all.size() <= total_connection); // ? can't ensure?
		for (DummyClient cli : all) {
			Assert.assertTrue(cli.closed.get() >= 1);
		}
	}

	@Test
	public void testOnError() {
		all.clear();
		fac.INST_CONNECT_TIMEOUT = new Duration(1, TimeUnit.SECONDS);
		final int total_connection = 50;
		final PooledClient<DummyClient> thepool = new PooledClient<DummyClient>(fac);
		Thread[] workers = new Thread[total_connection + 10];
		final CountDownLatch latch = new CountDownLatch(workers.length);
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						DummyClient cli = null;
						for (int i = 0; i < 1000; i++) {
							cli = thepool.getClient();
							if (cli == null || cli.errorOnUse) {
								thepool.releaseClient(cli);
							} else {
								thepool.retClient(cli);
							}
						}
					} finally {
						latch.countDown();
					}
				}

			});
			workers[i].start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("about to shutdown the pool");
		thepool.close(); // ensure no leak after pool shutdown
		System.out.println("Total created object = " + all.size());
		Assert.assertTrue(all.size() > 0);

		// ensure client is not created every time
		// Assert.assertTrue(all.size() < workers.length);
		// Assert.assertTrue(all.size() <= total_connection);
		for (DummyClient cli : all) {
			Assert.assertTrue(cli.closed.get() >= 1);
		}
	}

}

class DummyClient {

	public AtomicInteger closed = new AtomicInteger(0);
	public boolean errorOnUse = Math.random() > 0.5;

}

class DummyClientFactory extends AbstractClientFactory<DummyClient> {

	@Override
	public void release(DummyClient srv) {
		srv.closed.incrementAndGet();
	}

	@Override
	protected DummyClient doCreate(TProtocol inprot, TProtocol outprot) {
		DummyClient ret = new DummyClient();
		PooledClientTest.all.add(ret);
		return ret;
	}

	@Override
	public String getServiceName() {
		return DummyClient.class.getName();
	}

}