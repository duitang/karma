package com.duitang.service.karma.trace;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.After;
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
		ReporterSender.addZipkinSender("http://192.168.10.216");
		
		TracePoint tp = new TracePoint();
		tp.tc.group = "dev1";
		Thread.sleep(100);
		tp.close();
		
		ReporterSender.commitReports(Arrays.asList(tp.tc));
	}

	@Test
	public void testCommitReports() {
		fail("Not yet implemented");
	}

}

