package com.duitang.service.karma.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.zookeeper.ZKUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.TestingHosts;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.client.impl.RRRFactory;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCNode;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RPCRegistry;
import com.duitang.service.karma.support.RegistryInfo;
import com.duitang.service.karma.trace.TraceCell;

public class ZKClientListenerTest {

	final static String zk = TestingHosts.zk;

	ZKClientListener lsnr;
	IOBalanceFactory fac = new RRRFactory();
	RPCRegistry aware = new RPCRegistry();
	IOBalance b;

	@Before
	public void setUp() throws Exception {
		b = fac.createIOBalance(aware, RPCNodeHashing.createFromString(Arrays.asList("aa:11221", "bb:11122")));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReg() throws KarmaException {
		lsnr = new ZKClientListener();
		lsnr.register(b);
		Assert.assertTrue(lsnr.members.keySet().contains(b));
		lsnr.unregister(b);
		Assert.assertFalse(lsnr.members.keySet().contains(b));

		// repeatable
		lsnr.register(b);
		lsnr.register(b);
		Assert.assertTrue(lsnr.members.keySet().contains(b));
		lsnr.unregister(b);
		lsnr.unregister(b);
		Assert.assertFalse(lsnr.members.keySet().contains(b));
		lsnr.unregister(b);
		Assert.assertFalse(lsnr.members.keySet().contains(b));
	}

	@Test
	public void testUpdateAllNodesListOfString() throws KarmaException {
		lsnr = new ZKClientListener();
		List<String> nodes = Arrays.asList("a:111", "b:222", "c:333");
		IOBalanceDebuger io = new IOBalanceDebuger();
		lsnr.register(io);

		Assert.assertTrue(IOBalanceDebuger.debuger0.size() == 0);
		lsnr.updateAllNodes(nodes);
		Assert.assertTrue(IOBalanceDebuger.debuger0.size() == 1);

		List<String> lst = IOBalanceDebuger.debuger0.get(0);
		Assert.assertEquals(nodes.toString(), lst.toString());
	}

	@Test
	public void testUpdateAllNodesLinkedHashMapOfStringDouble() throws KarmaException {
		lsnr = new ZKClientListener();
		LinkedHashMap<String, Float> nodes = new LinkedHashMap<String, Float>();
		nodes.put("a:111", 1f);
		nodes.put("b:222", 2f);
		nodes.put("c:333", 3f);
		IOBalanceDebuger io = new IOBalanceDebuger();
		lsnr.register(io);

		Assert.assertTrue(IOBalanceDebuger.debuger1.size() == 0);
		lsnr.updateAllNodes(nodes);
		Assert.assertTrue(IOBalanceDebuger.debuger1.size() == 1);

		LinkedHashMap<String, Float> m = IOBalanceDebuger.debuger1.get(0);
		Assert.assertEquals(nodes.toString(), m.toString());
	}

	static void reg2RPC(ZKClusterWorker worker, boolean[] create) {
		Mock r1 = new Mock();
		r1.url = "aa:444";
		r1.online = create[0];
		r1.grp = "dev1";
		Mock r2 = new Mock();
		r2.url = "bb:555";
		r2.online = create[1];
		r2.grp = "dev1";
		worker.syncWrite(r1);
		worker.syncWrite(r2);
	}

	@Test
	public void testSyncPull() throws Exception {
		RPCNode.setHeartBeat(RPCNode.HEARTBEAT_PERIOD, 5);

		ZKClusterWorker worker = ZKClusterWorker.createInstance(zk);
		lsnr = worker.lsnr;

		Thread.sleep(1000);

		// force clear avoid for exceptions
		if (worker.zkCli.exists("/karma_rpc/nodes", null) != null) {
			ZKUtil.deleteRecursive(worker.zkCli, "/karma_rpc/nodes");
		}

		reg2RPC(worker, new boolean[] { true, true });

		ClusterMode mode = new ClusterMode();
		mode.nodes = new LinkedHashMap<String, Float>();
		mode.nodes.put("a:111", 1f);
		mode.nodes.put("b:222", 2f);
		mode.nodes.put("c:333", 3f);
		mode.freeze = true;
		worker.syncSetMode(mode);

		RegistryInfo info = lsnr.syncPull();
		System.out.println(info.isFreezeMode());
		System.out.println(info.getNodes());
		Assert.assertTrue(info.isFreezeMode());
		Assert.assertEquals(mode.nodes.toString(), info.getHashing().reverseToMap().toString());

		// clear freezing mode
		Assert.assertTrue(worker.syncClearMode());

		try {
			info = lsnr.syncPull();
			System.out.println("0. sync pull: " + info);
		} catch (Throwable t) {
			t.printStackTrace();
			Assert.fail();
		}
		System.out.println(info.isFreezeMode());
		System.out.println(info.getURLs());
		Assert.assertFalse(info.isFreezeMode());
		// wNodes is defined by all server , not sure here

		mode = new ClusterMode();
		mode.nodes = new LinkedHashMap<String, Float>();
		mode.nodes.put("a:111", 1f);
		mode.nodes.put("b:222", 2f);
		mode.nodes.put("c:333", 3f);
		mode.freeze = false;
		worker.syncSetMode(mode);

		info = lsnr.syncPull();
		Assert.assertNull(info);
		System.out.println("A. sync pull: " + info);

		reg2RPC(worker, new boolean[] { false, true });
		info = lsnr.syncPull();
		System.out.println("B. sync pull: " + info);
		Assert.assertNotNull(info);

		System.out.println(info.isFreezeMode());
		System.out.println(info.getURLs());
		Assert.assertFalse(info.isFreezeMode());
		Assert.assertTrue(info.getURLs().size() == 1);
		Assert.assertFalse(info.getURLs().contains("aa:444"));
		Assert.assertTrue(info.getURLs().contains("bb:555"));

		// if nodes not change will return null
		info = lsnr.syncPull();
		Assert.assertNull(info);

		worker.zkSR.service.clear();

		// Sleeping for MAX peroid
		for (int i = 0; i < RPCNode.MAX_LOSE_CONTACT; i++) {
			info = lsnr.syncPull();
			Assert.assertNull(info);
			System.out.println("C[" + i + "]. sync pull: " + info);
			System.out.println("C[" + i + "]. snap: " + lsnr.snap);
			Thread.sleep(RPCNode.HEARTBEAT_PERIOD - 10);
		}

		Thread.sleep(RPCNode.HEARTBEAT_PERIOD); // ensure
		RegistryInfo ret = worker.refreshRPCNodes();
		Assert.assertNotNull(ret);
		// every node is gone
		System.out.println("D. sync pull: " + ret);
		System.out.println("D. snap: " + lsnr.snap);
		ret = RegistryInfo.purged(false, ret.getHashing());
		Assert.assertNotNull(ret);
		System.out.println("E. purge: " + ret);
		System.out.println("E. snap: " + lsnr.snap);
		System.out.println(ret.getHashing().getDecays());
		Assert.assertNull(ret.getHashing().getDecays().get(0));

	}

}

class IOBalanceDebuger implements IOBalance {

	static List<List<String>> debuger0 = new ArrayList<>();
	static List<LinkedHashMap<String, Float>> debuger1 = new ArrayList<>();

	@Override
	public String next(String token) {
		return null;
	}

	@Override
	public void traceFeed(String token, TraceCell tc) {

	}

	@Override
	public void setNodes(List<String> nodes) {
		debuger0.add(nodes);
	}

	@Override
	public void setNodesWithWeights(LinkedHashMap<String, Float> nodes) {
		debuger1.add(nodes);
	}

	@Override
	public String getDebugInfo() {
		return null;
	}

}

class Mock implements RPCService {

	String url;
	boolean online;
	String grp;

	@Override
	public void start() throws KarmaException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRouter(Router router) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGroup(String grp) {
		// TODO Auto-generated method stub

	}

	@Override
	public Date getUptime() {
		return new Date();
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