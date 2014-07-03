package com.duitang.service.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.avro.ipc.Transceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractClientFactoryTest {

	protected AbstractClientFactory<Dummy> fac;
	protected String prot = "http://";
	protected String host = "127.0.0.1";
	protected int port1 = 8080;
	protected int port2 = 9090;
	protected String u = prot + host + ":" + port1 + ";" + prot + host + ":" + port2;
	protected ServerBootstrap folk1;
	protected ServerBootstrap folk2;

	@Before
	public void setUp() {
		fac = new MockFactory();
		try {
			folk1 = new ServerBootstrap();
			folk1.startUp(Dummy.class, new DummyService1(), port1);
			folk2 = new ServerBootstrap();
			folk2.startUp(Dummy.class, new DummyService1(), port2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void clearUp() {
		folk1.shutdown();
		folk2.shutdown();
	}

	@Test
	public void testSetUrl() {
		fac.setUrl(u + ";" + u + ";");
		System.out.println(fac.url);
		System.out.println(fac.sz);
		Assert.assertEquals(fac.serviceURL.size(), fac.sz);
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
		Set<String> dupurl = new HashSet<String>();
		for (String ur : u.split(";")) {
			dupurl.add(ur);
		}
		Assert.assertEquals(dupurl.size(), MockFactory.urls.size());
	}

}

class MockFactory extends AbstractClientFactory<Dummy> {

	public static List<Dummy> all = new ArrayList<Dummy>();
	public static List<Dummy> closed = new ArrayList<Dummy>();
	public static Set<String> urls = new HashSet<String>();

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

	@Override
	protected Dummy enhanceIt(Dummy client, Transceiver trans) {
		try {
			urls.add(trans.getRemoteName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return super.enhanceIt(client, trans);
	}

}