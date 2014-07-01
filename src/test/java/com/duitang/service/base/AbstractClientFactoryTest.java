package com.duitang.service.base;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.thrift.protocol.TProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractClientFactoryTest {

	protected AbstractClientFactory<MockDummy> fac;
	protected String host = "127.0.0.1";
	protected int port = 8080;
	protected String u = host + ":" + port;
	protected ServerSocket folk;

	@Before
	public void setUp() {
		fac = new MockFactory();
		try {
			folk = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@After
	public void clearUp() {
		try {
			folk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		Set<MockDummy> pool = new HashSet<MockDummy>();
		int sz = 10;
		MockDummy m = null;
		for (int i = 0; i < sz; i++) {
			m = fac.create();
			Assert.assertNotNull(m);
			pool.add(m);
		}
		Assert.assertEquals(sz, pool.size());
	}

}

class MockDummy {

	public AtomicInteger released = new AtomicInteger(0);
	public AtomicInteger created = new AtomicInteger(0);

}

class MockFactory extends AbstractClientFactory<MockDummy> {

	@Override
	public void release(MockDummy srv) {
		srv.released.incrementAndGet();
	}

	@Override
	protected MockDummy doCreate(TProtocol inprot, TProtocol outprot) {
		MockDummy ret = new MockDummy();
		ret.created.incrementAndGet();
		return ret;
	}

	@Override
	public String getServiceName() {
		return MockDummy.class.getName();
	}
}