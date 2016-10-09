package com.duitang.service.karma.cluster;

import static org.junit.Assert.fail;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.RPCService;

public class CuratorClusterWorkerTest {

	final static String conn = "192.168.10.216:2181";
	CuratorClusterWorker worker;
	ZKServerRegistry rs;
	ZKClientListener lsr;

	@Before
	public void setUp() throws Exception {
		rs = new ZKServerRegistry();
		lsr = new ZKClientListener();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateSimple() {
		worker = new CuratorClusterWorker(rs, lsr, conn);
		CuratorFramework cur = worker.createSimple();
		String name1 = cur.getState().name();
		Assert.assertFalse(cur.isStarted());
		cur.start();
		String name2 = cur.getState().name();
		Assert.assertTrue(cur.isStarted());

		Assert.assertNotSame(name1, name2);
	}

	@Test
	public void testSyncWrite() {
		worker = new CuratorClusterWorker(rs, lsr, conn);
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
		worker = new CuratorClusterWorker(rs, lsr, conn);
		MockRPCService rpc = new MockRPCService();
		// first write it
		boolean r = false;
		rpc.url = "tcp://192.168.1.223:8899";
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		rpc.url = "tcp://192.168.1.118:8899";
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		List<ClusterNode> lst = worker.syncRead();
		Assert.assertFalse(lst.isEmpty());
		Assert.assertTrue(lst.size() == 2);
		Assert.assertTrue(ClusterNode.class.getName().equals(lst.get(0).getClass().getName()));
		Assert.assertTrue(ClusterNode.class.getName().equals(lst.get(1).getClass().getName()));
	}

	@Test
	public void testSyncReadString() {
		worker = new CuratorClusterWorker(rs, lsr, conn);
		MockRPCService rpc = new MockRPCService();
		// first write it
		boolean r = false;
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		rpc.url = "tcp://192.168.1.118:8899";
		r = worker.syncWrite(rpc);
		Assert.assertTrue(r);

		worker = new CuratorClusterWorker(rs, lsr, conn);
		ClusterNode node = worker.syncRead("tcp://192.168.1.118:8899");

		Assert.assertEquals(node.url, rpc.url);
	}

	@Test
	public void testSyncClear() {
		worker = new CuratorClusterWorker(rs, lsr, conn);
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
		worker = new CuratorClusterWorker(rs, lsr, conn);
		ClusterMode mode = worker.syncGetMode();
		System.out.println(mode);
		System.out.println(mode.nodes);
		System.out.println(mode.freeze);
	}

	@Test
	public void testSyncSetMode() {
		worker = new CuratorClusterWorker(rs, lsr, conn);
		ClusterMode mode = new ClusterMode();
		mode.freeze = true;
		mode.nodes = new LinkedHashMap<>();
		mode.nodes.put("a", 1.0d);
		mode.nodes.put("b", 2.0d);
		worker.syncSetMode(mode);
	}

	@Test
	public void testSafePath() {
		String s ;
		worker = new CuratorClusterWorker(rs, lsr, conn);
		s = worker.safePath("tcp://192.168.0.1:23123");
		System.out.println(s);
		s = worker.safePath("192.168.0.1:23123");
		System.out.println(s);
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
