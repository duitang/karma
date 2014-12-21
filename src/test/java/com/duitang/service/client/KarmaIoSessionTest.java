package com.duitang.service.client;

import junit.framework.Assert;

import org.junit.Test;

public class KarmaIoSessionTest {

	@Test
	public void test() throws Exception {
		KarmaIoSession session;
		session = new KarmaIoSession("localhost:9999", 500);
		session.init();
		boolean ret = false;
		ret = session.isConnected();
		System.out.println(ret);
		Assert.assertTrue(ret);
		ret = session.isValid();
		System.out.println(ret);
		Assert.assertTrue(ret);
		session.close();
		ret = session.isConnected();
		System.out.println(ret);
		Assert.assertFalse(ret);
		ret = session.isValid();
		System.out.println(ret);
		Assert.assertFalse(ret);
	}

}
