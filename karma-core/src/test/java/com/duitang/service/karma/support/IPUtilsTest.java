package com.duitang.service.karma.support;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IPUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetHost() {
		String host = "aaaa";
		String h1, h2, h3, h4;
		h1 = IPUtils.getHost("tcp://" + host + ":1231");
		System.out.println(h1);
		h2 = IPUtils.getHost("" + host + ":1231");
		System.out.println(h2);
		h3 = IPUtils.getHost("tcp://" + host + "");
		System.out.println(h3);
		h4 = IPUtils.getHost("" + host + "");
		System.out.println(h4);
		Assert.assertEquals(h1, h2);
		Assert.assertEquals(h3, h4);
		Assert.assertEquals(h3, h2);
	}

	@Test
	public void testGetPort() {
		String host = "aaaa";
		Integer h1, h2, h3, h4;
		h1 = IPUtils.getPort("tcp://" + host + ":1231");
		System.out.println(h1);
		h2 = IPUtils.getPort("" + host + ":1231");
		System.out.println(h2);
		h3 = IPUtils.getPort("tcp://" + host + "");
		System.out.println(h3);
		h4 = IPUtils.getPort("" + host + "");
		System.out.println(h4);
		Assert.assertEquals(h1, h2);
		Assert.assertEquals(h3, h4);
	}

	@Test
	public void testGetIPAsInt() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testGetIPAsString() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testGetIp() {
		String ip = IPUtils.getIPAsString();
		System.out.println(ip);
	}

	@Test
	public void testPickUpIp() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testPickUpInetAddress() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testPickUpInetAddressNot() {
		Assert.fail("Not yet implemented");
	}

	@Test
	public void testPickUpIpNot() {
		Assert.fail("Not yet implemented");
	}

}
