package com.duitang.service.l2;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.avro.AvroRemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.base.PooledClient;
import com.duitang.service.base.ServerBootstrap;

public class L2ServiceTest {

	protected MockL2Service service = new MockL2Service();
	protected ServerBootstrap boot = new ServerBootstrap();
	protected PooledClient<L2Service> client;
	protected L2ServiceFactory fac;

	@Before
	public void setUp() throws Exception {
		boot.startUp(L2Service.class, service, 9090);
		fac = new L2ServiceFactory();
		fac.setUrl("http://localhost:9090");
		client = new PooledClient<L2Service>(fac);
	}

	@After
	public void tearDown() throws Exception {
		// Thread.sleep(1000 * 1000);
		client.close();
		boot.shutdown();
	}

	@Test
	public void testAll() throws AvroRemoteException {
		L2Service cli = client.getClient();
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
		Assert.assertEquals("helloworld", cli.session_get("").toString());
		Assert.assertTrue(cli.session_set("", ""));
		Assert.assertEquals(100, cli.session_expire("", 100));
		Assert.assertEquals(100, cli.session_delete(""));
		client.retClient(cli);
	}

}

class MockL2Service implements L2Service {

	@Override
	public boolean cat_setstring(CharSequence key, CharSequence value, int ttl) throws AvroRemoteException {
		return true;
	}

	@Override
	public boolean cat_addstring(CharSequence key, CharSequence value, int ttl) throws AvroRemoteException {
		return true;
	}

	@Override
	public long cat_incr(CharSequence key, long delta) throws AvroRemoteException {
		return 100 + delta;
	}

	@Override
	public CharSequence cat_getstring(CharSequence key) throws AvroRemoteException {
		return "helloworld";
	}

	@Override
	public boolean cat_delstring(CharSequence key) throws AvroRemoteException {
		return true;
	}

	@Override
	public Map<CharSequence, CharSequence> cat_mgetstring(CharSequence keys) throws AvroRemoteException {
		Map<CharSequence, CharSequence> mret = new HashMap<CharSequence, CharSequence>();
		mret.put("hello", "world");
		return mret;
	}

	@Override
	public Map<CharSequence, Object> session_getsession(CharSequence sessionid) throws AvroRemoteException {
		Map<CharSequence, Object> ret = new HashMap<CharSequence, Object>();
		ret.put("hello", "world");
		ret.put("0", 0);
		ret.put("1", 1L);
		ret.put("2", 2d);
		ret.put("3", 3f);
		return ret;
	}

	@Override
	public CharSequence session_get(CharSequence sessionid) throws AvroRemoteException {
		return "helloworld";
	}

	@Override
	public boolean session_set(CharSequence sessionid, CharSequence value) throws AvroRemoteException {
		return true;
	}

	@Override
	public long session_expire(CharSequence sessionid, int expiryage) throws AvroRemoteException {
		return 100;
	}

	@Override
	public long session_delete(CharSequence sessionid) throws AvroRemoteException {
		return 100;
	}

}
