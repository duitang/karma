package com.duitang.service.karma.trace;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TracePointTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTracePoint() throws IOException {
		TracePoint tp = new TracePoint();

		try {
			Thread.sleep(1100);
			tp.close();
		} catch (Exception e) {
			try {
				tp.close(e);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

		System.out.println(tp.tc);
		Assert.assertEquals(tp.tc.clazzName, TracePointTest.class.getName());
		Assert.assertEquals(tp.tc.name, "testTracePoint");

		tp = new TracePoint("aaa", "bbb");
		tp.setInfo("cwj", 12345, 56789L, "dev1");
		tp.close(new RuntimeException("err"));
		Assert.assertEquals(tp.tc.clazzName, "aaa");
		Assert.assertEquals(tp.tc.name, "bbb");
		Assert.assertTrue(tp.tc.err != null);
		Assert.assertTrue(tp.tc.pid == 56789L);
		Assert.assertEquals(tp.tc.host, "cwj");
		Assert.assertEquals(tp.tc.group, "dev1");
		Assert.assertTrue(tp.tc.port == 12345);

		System.out.println(tp.tc);

	}


}
