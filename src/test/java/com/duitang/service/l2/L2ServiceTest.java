package com.duitang.service.l2;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.avro.AvroRemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.base.ServerBootstrap;

public class L2ServiceTest {

	protected MockL2Service service = new MockL2Service();
	protected ServerBootstrap boot = new ServerBootstrap();
	protected L2ServiceV2Factory fac;

	@Before
	public void setUp() throws Exception {
		boot.startUp(L2ServiceV2.class, service, 9090);
		fac = new L2ServiceV2Factory();
		fac.setUrl("http://localhost:9090");
	}

	@After
	public void tearDown() throws Exception {
		Thread.sleep(1000 * 1000);
		boot.shutdown();
	}

	@Test
	public void testAll() throws AvroRemoteException {
		L2ServiceV2 cli = fac.create();
		Assert.assertTrue(cli.cat_setstring("", "", 1));
		Assert.assertTrue(cli.cat_addstring("", "", 1));
		Assert.assertTrue(cli.cat_incr("", 1) == 101);
		Assert.assertEquals("helloworld", cli.cat_getstring("").toString());
		Assert.assertTrue(cli.cat_delstring(""));
		Map m = new HashMap();
		m.put("hello", "world");
		String lst = "1\n2\n";
		Assert.assertEquals(m.toString(), cli.cat_mgetstring(lst).toString());
		Assert.assertFalse(cli.session_getsession("").isEmpty());
		Assert.assertTrue(cli.session_setsession("", new HashMap()));
		Assert.assertEquals("helloworld", cli.session_get("").toString());
		Assert.assertTrue(cli.session_set("", ""));
		Assert.assertEquals(100, cli.session_expire("", 100));
		Assert.assertEquals(100, cli.session_delete(""));
		Assert.assertEquals("helloworld", cli.session_genId());
		fac.release(cli);
	}

}

class MockL2Service implements L2Service, L2ServiceV2 {

	@Override
	public boolean cat_setstring(String key, String value, int ttl) throws AvroRemoteException {
		return true;
	}

	@Override
	public boolean cat_addstring(String key, String value, int ttl) throws AvroRemoteException {
		return true;
	}

	@Override
	public long cat_incr(String key, long delta) throws AvroRemoteException {
		return 100 + delta;
	}

	@Override
	public String cat_getstring(String key) throws AvroRemoteException {
		return "helloworld";
	}

	@Override
	public boolean cat_delstring(String key) throws AvroRemoteException {
		return true;
	}

	@Override
	public Map<String, String> cat_mgetstring(String keys) throws AvroRemoteException {
		Map<String, String> mret = new HashMap<String, String>();
		mret.put("hello", "world");
		return mret;
	}

	@Override
	public Map<String, Object> session_getsession(String sessionid) throws AvroRemoteException {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("hello", "world");
		ret.put("0", 0);
		ret.put("1", 1L);
		ret.put("2", 2d);
		ret.put("3", 3f);
		return ret;
	}

	@Override
	public String session_get(String sessionid) throws AvroRemoteException {
		return "helloworld";
	}

	@Override
	public boolean session_set(String sessionid, String value) throws AvroRemoteException {
		return true;
	}

	@Override
	public long session_expire(String sessionid, int expiryage) throws AvroRemoteException {
		return 100;
	}

	@Override
	public long session_delete(String sessionid) throws AvroRemoteException {
		return 100;
	}

	@Override
	public String session_genId() throws AvroRemoteException {
		return "helloworld";
	}

	@Override
	public boolean session_setsession(String sessionid, Map<String, Object> sessiondata) throws AvroRemoteException {
		return true;
	}

}
