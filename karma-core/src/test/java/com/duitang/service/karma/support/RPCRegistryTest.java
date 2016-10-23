package com.duitang.service.karma.support;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.impl.RRRFactory;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.AsyncRegistryWriter;
import com.duitang.service.karma.server.RPCService;

public class RPCRegistryTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws KarmaException {
		RPCNodeHashing hashing = RPCNodeHashing.createFromString(Arrays.asList("aa:11", "bb:22"));

		RPCRegistry rs = new RPCRegistry();
		RRRFactory fac = new RRRFactory();
		rs.setFactory(fac);
		Assert.assertTrue(rs.getFactory() == fac);

		IOBalance ba = fac.createIOBalance(rs, hashing);
		DemoRPC rpc = new DemoRPC();

		rs.addReaders(Arrays.asList((AsyncRegistryReader) new DemoReader()));
		rs.addWriters(Arrays.asList((AsyncRegistryWriter) new DemoWriter()));

		rs.registerRead(ba);
		Assert.assertTrue(readers.get() == 1);
		rs.registerWrite(rpc);
		Assert.assertTrue(writers.get() == 1);

		rs.unRegisterRead(ba);
		Assert.assertTrue(readers.get() == 0);
		rs.unRegisterWrite(rpc);
		Assert.assertTrue(writers.get() == 0);

		System.out.println(rs.getInfo());
		Assert.assertNotNull(rs.getInfo());
	}

	static class DemoRPC implements RPCService {

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
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getGroup() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceURL() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServiceProtocol() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean online() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	static AtomicInteger readers = new AtomicInteger(0);
	static AtomicInteger writers = new AtomicInteger(0);
	static AtomicInteger syncPull = new AtomicInteger(0);
	static AtomicInteger syncPush = new AtomicInteger(0);

	static class DemoReader implements AsyncRegistryReader {

		@Override
		public void register(IOBalance balancer) throws KarmaException {
			readers.incrementAndGet();
		}

		@Override
		public void unregister(IOBalance balancer) throws KarmaException {
			readers.decrementAndGet();
		}

		@Override
		public RegistryInfo syncPull() {
			syncPull.incrementAndGet();
			return null;
		}

	}

	static class DemoWriter implements AsyncRegistryWriter {

		@Override
		public void register(RPCService rpc) throws KarmaException {
			writers.incrementAndGet();
		}

		@Override
		public void unregister(RPCService rpc) throws KarmaException {
			writers.decrementAndGet();
		}

		@Override
		public void syncPush(RPCService rpc) {
			syncPush.incrementAndGet();
		}

	}

	@Test
	public void test2() {
		RPCNodeHashing h_new = RPCNodeHashing.createFromString(Arrays.asList("aa:111", "bb:222"));
		RPCNodeHashing h_snap = RPCNodeHashing.createFromString(Arrays.asList("aa:111", "bb:222", "cc:333"));

		// testing for 2 period
		for (int i = 0; i < h_new.nodes.size(); i++) {
			RPCNode node = h_new.nodes.get(i);
			node.heartbeat = System.currentTimeMillis() - RPCNode.HEART_BEAT_PERIOD + 300;
		}
		for (int i = 0; i < h_snap.nodes.size(); i++) {
			RPCNode node = h_snap.nodes.get(i);
			node.heartbeat = System.currentTimeMillis() - 2 * RPCNode.HEART_BEAT_PERIOD + 300;
		}
		RegistryInfo info = new RegistryInfo(false, h_new);
		RPCNodeHashing h_ret = info.calcDecayFactor(h_snap);
		System.out.println(h_ret.decays);
		int pos = h_ret.urls.indexOf("cc:333");
		Assert.assertTrue(h_ret.decays.get(pos) < (1d / 4 + 0.01) && h_ret.decays.get(pos) > 0);

		// testing for very very little, just dying
		for (int i = 0; i < h_new.nodes.size(); i++) {
			RPCNode node = h_new.nodes.get(i);
			node.heartbeat = System.currentTimeMillis() - RPCNode.HEART_BEAT_PERIOD + 300;
		}
		for (int i = 0; i < h_snap.nodes.size(); i++) {
			RPCNode node = h_snap.nodes.get(i);
			node.heartbeat = System.currentTimeMillis() - (RPCNode.MAX_LOSE_CONTACT) * RPCNode.HEART_BEAT_PERIOD + 300;
		}
		info = new RegistryInfo(false, h_new);
		h_ret = info.calcDecayFactor(h_snap);
		System.out.println(h_ret.decays);
		pos = h_ret.urls.indexOf("cc:333");
		Assert.assertTrue(h_ret.decays.get(pos) < (1d / Math.pow(RPCNode.MAX_LOSE_CONTACT, 2) + 0.01)
				&& h_ret.decays.get(pos) > 0);

		// testing for died
		for (int i = 0; i < h_new.nodes.size(); i++) {
			RPCNode node = h_new.nodes.get(i);
			node.heartbeat = System.currentTimeMillis() - RPCNode.HEART_BEAT_PERIOD + 300;
		}
		for (int i = 0; i < h_snap.nodes.size(); i++) {
			RPCNode node = h_snap.nodes.get(i);
			node.heartbeat = System.currentTimeMillis() - RPCNode.MAX_LOSE_CONTACT * RPCNode.HEART_BEAT_PERIOD - 2;
		}
		info = new RegistryInfo(false, h_new);
		h_ret = info.calcDecayFactor(h_snap);
		System.out.println(h_ret.decays);
		pos = h_ret.urls.indexOf("cc:333");
		Assert.assertNull(h_ret.decays.get(pos));

		info = RegistryInfo.purged(info.freezeMode, h_ret);
		System.out.println(info.getNodes());
		Assert.assertTrue(info.getNodes().size() == 2);
		Assert.assertTrue(info.getURLs().contains("aa:111"));
		Assert.assertTrue(info.getURLs().contains("bb:222"));
		Assert.assertFalse(info.getURLs().contains("cc:333"));

	}

}
