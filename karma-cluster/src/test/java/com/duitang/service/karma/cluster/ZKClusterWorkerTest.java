package com.duitang.service.karma.cluster;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.TestingHosts;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCNode;

public class ZKClusterWorkerTest {

	final static String conn = TestingHosts.zk;
	// final static String conn = "192.168.10.216:2181";
	ZKClusterWorker worker;
	ZKServerRegistry rs;
	ZKClientListener lsr;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSyncWrite() {
		worker = ZKClusterWorker.createInstance(conn);
		MockRPCService rpc = new MockRPCService();
		// first write it
		boolean r = false;
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		// try {
		// System.in.read(new byte[1]);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// then clear it
		r = worker.syncClearRPCNode(rpc);
		Assert.assertTrue(r);
	}

	@Test
	public void testSyncRead() {
		worker = ZKClusterWorker.createInstance(conn);
		MockRPCService rpc = new MockRPCService();
		// first write it
		boolean r = false;
		rpc.url = "tcp://192.168.1.223:8899";
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		rpc.url = "tcp://192.168.1.118:8899";
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		List<RPCNode> lst = worker.syncRead();
		Assert.assertFalse(lst.isEmpty());
		Assert.assertTrue(lst.size() == 2);
		Assert.assertTrue(RPCNode.class.getName().equals(lst.get(0).getClass().getName()));
		Assert.assertTrue(RPCNode.class.getName().equals(lst.get(1).getClass().getName()));
	}

	@Test
	public void testSyncReadString() {
		worker = ZKClusterWorker.createInstance(conn);
		MockRPCService rpc = new MockRPCService();
		// first write it
		boolean r = false;
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		rpc.url = "192.168.1.118:8899";
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		// worker = new ZKClusterWorker(rs, lsr, conn);
		RPCNode node = worker.syncRead("192.168.1.118:8899");

		System.out.println(node.url);
		System.out.println(rpc.url);
		Assert.assertEquals(node.url, rpc.url);
	}

	@Test
	public void testSyncClear() {
		worker = ZKClusterWorker.createInstance(conn);
		MockRPCService rpc = new MockRPCService();
		// first write it
		boolean r = false;
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		r = worker.syncClearRPCNode(rpc);
		Assert.assertTrue(r);
	}

	@Test
	public void testSyncGetMode() {
		worker = ZKClusterWorker.createInstance(conn);
		ClusterMode mode = worker.syncGetMode();
		System.out.println(mode);
		System.out.println(mode.nodes);
		System.out.println(mode.freeze);
	}

	@Test
	public void testSyncSetMode() {
		worker = ZKClusterWorker.createInstance(conn);
		ClusterMode mode = new ClusterMode();
		mode.freeze = true;
		mode.nodes = new LinkedHashMap<>();
		mode.nodes.put("a:11", 1.0d);
		mode.nodes.put("b:22", 2.0d);
		worker.syncSetMode(mode);
	}

}

class MockRPCService implements RPCService {

	String grp;
	Date created = new Date();
	String url = "tcp://192.168.1.123:8899";
	boolean online;

	@Override
	public void start() throws KarmaException {

	}

	@Override
	public void stop() {

	}

	@Override
	public void setRouter(Router router) {

	}

	@Override
	public void setGroup(String grp) {
		this.grp = grp;
	}

	@Override
	public Date getUptime() {
		return created;
	}

	@Override
	public String getGroup() {
		return grp;
	}

	@Override
	public String getServiceURL() {
		return url;
	}

	@Override
	public String getServiceProtocol() {
		return "tcp";
	}

	@Override
	public boolean online() {
		return online;
	}

}
