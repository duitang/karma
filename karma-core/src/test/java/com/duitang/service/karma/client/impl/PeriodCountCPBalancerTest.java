package com.duitang.service.karma.client.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.support.RPCUrls;
import com.duitang.service.karma.trace.TraceCell;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class PeriodCountCPBalancerTest {

	static List<String> nodes = Arrays.asList("localhost:8888", "127.0.0.1:8888");
	PeriodCountCPBalancer balancer = null;

	@Before
	public void setUp() throws Exception {
		balancer = new PeriodCountCPBalancer(nodes);
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void testHitPoint() throws Throwable {
		// 5s
		balancer = new PeriodCountCPBalancer(nodes, 5 * 1000, 0, false);
		for (int i = 0; i < 3; i++) {
			try {
				Thread.sleep(5010); // approximate
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Assert.assertTrue(balancer.hitPoint());
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 1000; j++) {
				balancer.next(null);
			}
			Assert.assertFalse(balancer.hitPoint());
		}

		// 100 hit
		balancer = new PeriodCountCPBalancer(nodes, 0, 100, false);
		for (int i = 0; i < 3; i++) {

			for (int j = 0; j < 95; j++) {
				balancer.next(null);
			}
			Assert.assertFalse(balancer.hitPoint());
			for (int j = 0; j < 5; j++) {
				balancer.next(null);
			}
			Assert.assertTrue(balancer.hitPoint());
		}
		Thread.sleep(60 * 1000);
		Assert.assertFalse(balancer.hitPoint());

		// 5s or 100hit
		balancer = new PeriodCountCPBalancer(nodes, 5 * 1000, 100, false);
		for (int i = 0; i < 3; i++) {
			Thread.sleep(5010);
			Assert.assertTrue(balancer.hitPoint());
			for (int j = 0; j < 95; j++) {
				balancer.next(null);
				Assert.assertFalse(balancer.hitPoint());
			}
			for (int j = 0; j < 5; j++) {
				balancer.next(null);
			}
			Assert.assertTrue(balancer.hitPoint());
		}

		// 5s and 100hit
		balancer = new PeriodCountCPBalancer(nodes, 5 * 1000, 100, true);
		for (int i = 0; i < 3; i++) {
			Thread.sleep(5010);
			Assert.assertFalse(balancer.hitPoint());
			for (int j = 0; j < 95; j++) {
				balancer.next(null);
				Assert.assertFalse(balancer.hitPoint());
			}
			for (int j = 0; j < 5; j++) {
				balancer.next(null);
			}
			Assert.assertTrue(balancer.hitPoint());
		}

	}

	@Test
	public void testNextAndTraceFeed() throws Throwable {
		Logger lg = (Logger) LoggerFactory.getLogger(AutoReBalance.class);
		lg.setLevel(Level.DEBUG);
		// only trace feed action validation is needed
		// not here for correctness because of policy unit-test
		List<String> cfg = Arrays.asList("a:9999", "b:9999", "c:9999");

		int count = 10000;
		balancer = new PeriodCountCPBalancer(cfg, 0, count, false);

		AutoReBalance arb = null;
		Field f = null;
		Candidates cdd = null;

		arb = (AutoReBalance) balancer.nap.policy;
		f = arb.getClass().getDeclaredField("cdd");
		f.setAccessible(true);
		cdd = (Candidates) f.get(balancer.nap.policy);
		System.out.println(Arrays.toString(cdd.choice));
		Random r = new Random(Double.valueOf(Math.random() * 10000000).longValue());

		TraceCell tc = null;
		String key = null;
		int base = (count / 3 + 1);
		int loop = 20;
		for (int j = 0; j < loop * base; j++) {
			key = cfg.get(0);
			balancer.next(null);

			tc = new TraceCell(false, null, null);
			tc.duration = Double.valueOf(0.05d * r.nextDouble() * 1000000).longValue();
			tc.successful = true;
			balancer.traceFeed(key, tc);

			key = cfg.get(1);
			balancer.next(null);
			tc = new TraceCell(false, null, null);
			tc.duration = Double.valueOf(0.2d * r.nextDouble() * 1000000).longValue();
			tc.successful = true;
			balancer.traceFeed(key, tc);

			key = cfg.get(2);
			balancer.next(null);
			tc = new TraceCell(false, null, null);
			tc.duration = Double.valueOf(0.5d * r.nextDouble() * 1000000).longValue();
			tc.successful = true;
			balancer.traceFeed(key, tc);

		}

	}

	@Test
	public void testSetNodesWithWeights() {
		LinkedHashMap<String, Double> r = new LinkedHashMap<>();
		r.put("a:9999", 1.0d);
		r.put("b:9999", 3.0d);
		r.put("c:9999", 2.0d);
		RPCUrls cfg = new RPCUrls(r);
		balancer = new PeriodCountCPBalancer(nodes);
		balancer.setNodes(cfg.getNodes());
		Assert.assertNotEquals(balancer.nap.nodes, cfg);

		balancer.syncReload();
		Assert.assertEquals(balancer.nap.nodes, cfg);
		Assert.assertTrue(balancer.nap.policy.size() == cfg.getNodes().size());
	}

	@Test
	public void testSetNodes() {
		RPCUrls cfg = new RPCUrls(Arrays.asList("a:9999", "b:9999", "c:9999"));
		balancer = new PeriodCountCPBalancer(nodes);
		balancer.setNodes(cfg.getNodes());
		Assert.assertNotEquals(balancer.nap.nodes, cfg);

		balancer.syncReload();
		Assert.assertEquals(balancer.nap.nodes, cfg);
		Assert.assertTrue(balancer.nap.policy.size() == cfg.getNodes().size());
	}

	@Test
	public void testGetSafeNodesLinkedHashMapOfStringDouble() {
		LinkedHashMap<String, Double> m = new LinkedHashMap<>();
		List<String> s = Arrays.asList(new String[] { "aaa:123", "tcp://bbb:456", "aaa:789" });
		for (String ss : s) {
			m.put(ss, 1.0d);
		}
		RPCUrls u = PeriodCountCPBalancer.getSafeNodes(m);
		System.out.println(u.getNodes());
		Assert.assertTrue(s.size() == u.getNodes().size());
		for (String ss : s) {
			Assert.assertTrue(u.getNodes().contains(RPCUrls.getRawConnURL(ss)));
		}
	}

}
