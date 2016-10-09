package com.duitang.service.karma.cluster;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.duitang.service.karma.support.ClusterRegistry;
import com.duitang.service.karma.trace.TraceCell;

public class ZKClientListenerTest {

	ZKClientListener lsnr;
	IOBalanceFactory fac = new RRRFactory();
	ClusterRegistry aware = new ClusterRegistry();
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
		fail("Not yet implemented");
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
