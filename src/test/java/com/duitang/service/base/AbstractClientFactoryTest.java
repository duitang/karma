package com.duitang.service.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractClientFactoryTest {

	protected AbstractClientFactory<Dummy> fac;
	protected String prot = "http://";
	protected String host = "127.0.0.1";
	protected int port = 8080;
	protected String u = prot + host + ":" + port;
	protected ServerBootstrap folk;

	@Before
	public void setUp() {
		fac = new MockFactory();
		try {
			folk = new ServerBootstrap();
			folk.startUp(Dummy.class, new DummyService1(), port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void clearUp() {
		folk.shutdown();
	}

	@Test
	public void testSetUrl() {
		fac.setUrl(u);
		Assert.assertEquals(host, fac.getHost());
		Assert.assertEquals(port, fac.getPort());
	}

	@Test
	public void testDoCreate() {
		fac.setUrl(u);
		Set<Dummy> pool = new HashSet<Dummy>();
		int sz = 10;
		Dummy m = null;
		for (int i = 0; i < sz; i++) {
			m = fac.create();
			Assert.assertNotNull(m);
			pool.add(m);
		}
		Assert.assertEquals(sz, pool.size());
	}

}

class MockFactory extends AbstractClientFactory<Dummy> {

	public static List<Dummy> all = new ArrayList<Dummy>();
	public static List<Dummy> closed = new ArrayList<Dummy>();

	@Override
	public void release(Dummy srv) {
		super.release(srv);
		closed.add(srv);
	}

	@Override
	public Dummy create() {
		Dummy ret = super.create();
		all.add(ret);
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