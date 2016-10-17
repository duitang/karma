package com.duitang.service.karma.support;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NameUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetHostname() {
		String name = NameUtil.getHostname();
		Assert.assertNotNull(name);
	}

	@Test
	public void testSetAppName() {
		NameUtil.setHostname("ababab");
		Assert.assertEquals(NameUtil.getHostname(), "ababab");

		NameUtil.setAppName("apapap");
		Assert.assertEquals(NameUtil.getInstanceTag().app, "apapap");
		Assert.assertEquals(NameUtil.getInstanceTag().host, "ababab");

		try {
			NameUtil.setHostname(null);
			Assert.fail();
		} catch (Exception e) {
		}
		
		try {
			NameUtil.setAppName(null);
			Assert.fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void testGenClientIdFromCode() {
		String name = NameUtil.genClientIdFromCode();
		Assert.assertEquals("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner", name);
	}

}
