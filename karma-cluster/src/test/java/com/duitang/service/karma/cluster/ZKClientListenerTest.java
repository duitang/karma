package com.duitang.service.karma.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.client.impl.RRRFactory;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCRegistry;
import com.duitang.service.karma.support.RegistryInfo;
import com.duitang.service.karma.trace.TraceCell;

public class ZKClientListenerTest {

	ZKClientListener lsnr;
	IOBalanceFactory fac = new RRRFactory();
	RPCRegistry aware = new RPCRegistry();
	IOBalance b;

	@Before
	public void setUp() throws Exception {
		b = fac.createIOBalance(aware, Arrays.asList("aa:11221", "bb:11122"));
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
		LinkedHashMap<String, Double> nodes = new LinkedHashMap<String, Double>();
		nodes.put("a:111", 1d);
		nodes.put("b:222", 2d);
		nodes.put("c:333", 3d);
		IOBalanceDebuger io = new IOBalanceDebuger();
		lsnr.register(io);

		Assert.assertTrue(IOBalanceDebuger.debuger1.size() == 0);
		lsnr.updateAllNodes(nodes);
		Assert.assertTrue(IOBalanceDebuger.debuger1.size() == 1);

		LinkedHashMap<String, Double> m = IOBalanceDebuger.debuger1.get(0);
		Assert.assertEquals(nodes.toString(), m.toString());
	}

	@Test
	public void testSyncPull() {
		CuratorClusterWorker worker = CuratorClusterWorker.createInstance("192.168.10.216:2181");
		lsnr = worker.lsnr;

		ClusterMode mode = new ClusterMode();
		mode.nodes = new LinkedHashMap<String, Double>();
		mode.nodes.put("a:111", 1d);
		mode.nodes.put("b:222", 2d);
		mode.nodes.put("c:333", 3d);
		mode.freeze = true;
		worker.syncSetMode(mode);

		RegistryInfo info = lsnr.syncPull();
		System.out.println(info.freezeMode);
		System.out.println(info.wNodes);
		Assert.assertTrue(info.freezeMode);
		Assert.assertEquals(mode.nodes.toString(), info.wNodes.toString());

		Assert.assertTrue(worker.syncClearMode());
		info = lsnr.syncPull();
		System.out.println(info.freezeMode);
		System.out.println(info.wNodes);
		Assert.assertFalse(info.freezeMode);
		// wNodes is defined by all server , not sure here

		mode = new ClusterMode();
		mode.nodes = new LinkedHashMap<String, Double>();
		mode.nodes.put("a:111", 1d);
		mode.nodes.put("b:222", 2d);
		mode.nodes.put("c:333", 3d);
		mode.freeze = false;
		worker.syncSetMode(mode);

		Mock r1 = new Mock();
		r1.url = "aa:444";
		r1.online = true;
		r1.grp = "dev1";
		worker.syncWrite(r1);
		r1 = new Mock();
		r1.url = "bb:555";
		r1.online = true;
		r1.grp = "dev1";
		worker.syncWrite(r1);
		info = lsnr.syncPull();
		System.out.println(info.freezeMode);
		System.out.println(info.wNodes);
		Assert.assertFalse(info.freezeMode);
		Assert.assertTrue(info.wNodes.size() == 2);
		Assert.assertTrue(info.wNodes.containsKey("aa:444"));
		Assert.assertTrue(info.wNodes.containsKey("bb:555"));

	}

}

class IOBalanceDebuger implements IOBalance {

	static List<List<String>> debuger0 = new ArrayList<>();
	static List<LinkedHashMap<String, Double>> debuger1 = new ArrayList<>();

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
	public void setNodesWithWeights(LinkedHashMap<String, Double> nodes) {
		debuger1.add(nodes);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean online() {
		return online;
	}

}