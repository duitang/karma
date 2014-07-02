package com.duitang.service.base;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import junit.framework.Assert;

import org.apache.avro.AvroRemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PooledClientTest {

	public static Set<Dummy> all = new HashSet<Dummy>();
	public static Set<Dummy> closed = new HashSet<Dummy>();
	protected DummyClientFactory1 fac;
	protected PooledClient<Dummy> pool;
	protected ServerBootstrap folk;
	protected int capacity;
	protected int idle;

	@Before
	public void setUp() {
		all.clear();
		capacity = 50;
		idle = capacity;
		fac = new DummyClientFactory1();
		fac.setUrl("http://localhost:8080");
		pool = new PooledClient<Dummy>(fac, capacity, idle);
		try {
			folk = new ServerBootstrap();
			folk.startUp(Dummy.class, new DummyService1(), 8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void clearUp() {
		try {
			folk.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSimpleGetRet() {
		Set<Dummy> checker = new HashSet<Dummy>();
		int sz = capacity;
		Dummy cli;
		for (int i = 0; i < sz; i++) {
			cli = pool.getClient();
			Assert.assertNotNull(cli);
			Assert.assertFalse(checker.contains(cli)); // not in used
			checker.add(cli);
		}
		for (Dummy cl : checker) {
			pool.retClient(cl);
		}
		for (int i = 0; i < sz; i++) {
			cli = pool.getClient();
			Assert.assertNotNull(cli);
			Assert.assertTrue(checker.contains(cli)); // already allocated
		}
		for (Dummy cl : checker) {
			pool.retClient(cl);
		}
	}

	@Test
	public void testNoRelease() {
		all.clear();
		final int total_connection = 30;
		final PooledClient<Dummy> thepool = new PooledClient<Dummy>(fac);
		Thread[] workers = new Thread[total_connection + 10];
		final CountDownLatch latch = new CountDownLatch(workers.length);
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Dummy cli = null;
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
		for (Dummy cli : all) {
			Assert.assertTrue(PooledClientTest.closed.contains(cli));
		}
	}

	@Test
	public void testOnError() {
		all.clear();
		final int total_connection = 50;
		final PooledClient<Dummy> thepool = new PooledClient<Dummy>(fac);
		Thread[] workers = new Thread[total_connection + 10];
		final CountDownLatch latch = new CountDownLatch(workers.length);
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Dummy cli = null;
						boolean err = false;
						for (int i = 0; i < 1000; i++) {
							cli = thepool.getClient();
							try {
								cli.dummy_dummy();
							} catch (AvroRemoteException e) {
								err = true;
							}
							if (cli == null || err) {
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
		System.out.println(PooledClientTest.closed.size());
		for (Dummy cli : all) {
			Assert.assertTrue(PooledClientTest.closed.contains(cli));
		}
	}
}

class DummyService1 implements Dummy {

	@Override
	public Object dummy_dummy() throws AvroRemoteException {
		boolean errorOnUse = Math.random() > 0.5;
		if (errorOnUse) {
			throw new AvroRemoteException("fuck u!");
		}
		return null;
	}

}

class DummyClientFactory1 extends AbstractClientFactory<Dummy> {

	@Override
	public void release(Dummy srv) {
		Dummy cli = (Dummy) srv;
		super.release(cli);
		PooledClientTest.closed.add(cli);
	}

	@Override
	public Dummy create() {
		Dummy ret = (Dummy) super.create();
		PooledClientTest.all.add(ret);
		return ret;
	}

	@Override
	public String getServiceName() {
		return Dummy.class.getName();
	}

	@Override
	public Class getServiceType() {
		return Dummy.class;
	}

}