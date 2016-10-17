package com.duitang.service.karma.support;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeamonJobsTest {

	static AtomicInteger count = new AtomicInteger(0);
	static int ii = 5;

	static class Foobar implements Runnable {

		@Override
		public void run() {
			count.incrementAndGet();
		}

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPickName() {
		String name = DaemonJobs.pickName(new Foobar());
		Assert.assertEquals(name, "Karma-Job-com.duitang.service.karma.support.DeamonJobsTest$Foobar");
	}

	@Test
	public void testRunJob() throws Exception {

		for (int i = 0; i < ii; i++) {
			DaemonJobs.runJob(new Foobar());
		}
		Thread.sleep(1000);
		Assert.assertTrue(count.get() == ii);
	}

	@Test
	public void testShutdown() throws Exception {
		testRunJob();
		Thread.sleep(1000);
		DaemonJobs.shutdown();
		Thread.sleep(1000);
		Assert.assertTrue(DaemonJobs.jobs.isTerminated());
		DaemonJobs.shutdown();
	}

	@Test
	public void testReset() throws Exception {
		testShutdown();

		DaemonJobs.reset();
		Thread.sleep(1000);

		Assert.assertTrue(count.get() == ii * 2);
	}

}
