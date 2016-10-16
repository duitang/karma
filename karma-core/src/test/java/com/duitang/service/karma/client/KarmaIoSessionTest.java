package com.duitang.service.karma.client;

import org.junit.Assert;
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
		ret = session.isAlive();
		System.out.println(ret);
		Assert.assertTrue(ret);
		session.close();
		ret = session.isConnected();
		System.out.println(ret);
		Assert.assertFalse(ret);
		ret = session.isAlive();
		System.out.println(ret);
		Assert.assertFalse(ret);
	}

}
