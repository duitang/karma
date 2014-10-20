package com.duitang.service.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.avro.AvroRemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractClientFactoryTest {

	protected ClientFactory<Dummy> fac;
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
			StringBuilder sb = new StringBuilder();
			sb.setLength(0);
			folk1.serviceInfo(Dummy.class, sb);
			System.out.println(sb.toString());
			folk2 = new ServerBootstrap();
			folk2.startUp(Dummy.class, new DummyService1(), port2);
			sb.setLength(0);
			folk2.serviceInfo(Dummy.class, sb);
			System.out.println(sb.toString());
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
		List<Dummy> pool = new ArrayList<Dummy>();
		int sz = 10;
		Dummy m = null;
		for (int i = 0; i < sz; i++) {
			m = fac.create();
			Assert.assertNotNull(m);
			pool.add(m);
			fac.release(m);
		}
		Assert.assertEquals(sz, pool.size());
		Set<String> dupurl = new HashSet<String>();
		for (String ur : u.split(";")) {
			dupurl.add(ur);
		}
		Assert.assertEquals(dupurl.size(), 2);
		Assert.assertEquals(MockFactory.closed.size(), sz);
	}

}

class MockFactory extends ClientFactory<Dummy> {

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

class DummyService1 implements Dummy {

	@Override
	public Void dummy_dummy() throws AvroRemoteException {
		boolean errorOnUse = Math.random() > 0.5;
		if (errorOnUse) {
			throw new AvroRemoteException("fuck u!");
		}
		return null;
	}

}
