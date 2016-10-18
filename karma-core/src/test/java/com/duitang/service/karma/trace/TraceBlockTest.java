package com.duitang.service.karma.trace;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TraceBlockTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTraceBlock() throws Exception {
		TraceContextHolder.setSampler(new AlwaysSampled());

		TraceBlock tb = new TraceBlock();
		tb.close();

		try {
			Thread.sleep(1100);
			tb.close();
		} catch (Exception e) {
			tb.close(e);
		}

		System.out.println(tb.tc);
		Assert.assertEquals(tb.tc.clazzName, TraceBlockTest.class.getName());
		Assert.assertEquals(tb.tc.name, "testTraceBlock");

		tb = new TraceBlock("aaa", "bbb");
		tb.setInfo("cwj", 12345, 56789L, "dev1");
		tb.setAttribute("foo", "bar");
		tb.close(new RuntimeException("err"));
		Assert.assertEquals(tb.tc.clazzName, "aaa");
		Assert.assertEquals(tb.tc.name, "bbb");
		Assert.assertTrue(tb.tc.err != null);
		Assert.assertTrue(tb.tc.pid == 56789L);
		Assert.assertEquals(tb.tc.host, "cwj");
		Assert.assertEquals(tb.tc.group, "dev1");
		Assert.assertTrue(tb.tc.port == 12345);
		Assert.assertEquals(tb.getAttribute("foo"), "bar");

		System.out.println(tb.tc);

	}

}
