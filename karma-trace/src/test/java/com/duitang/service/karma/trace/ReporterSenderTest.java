package com.duitang.service.karma.trace;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReporterSenderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddZipkinSender() throws Exception {
		TraceContextHolder.alwaysSampling();

		Assert.assertTrue(ReporterSender.senders.isEmpty());
		ReporterSender.addZipkinSender("http://192.168.10.216:9411");
		Assert.assertFalse(ReporterSender.senders.isEmpty());
		Thread.sleep(3000);

		TracePoint tp = new TracePoint();
		tp.tc.group = "dev1";
		Thread.sleep(100);
		tp.close();

		ReporterSender.commitReports(Arrays.asList(tp.tc));

		Thread.sleep(1000);
	}

}
