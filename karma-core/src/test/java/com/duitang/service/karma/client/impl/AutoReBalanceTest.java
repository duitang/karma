package com.duitang.service.karma.client.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AutoReBalanceTest {

	AutoReBalance auto;

	@Before
	public void setUp() throws Exception {
		auto = new AutoReBalance(5);
		auto.reload(new float[] { 0.1f, 0.1f, 0.1f, 0.1f, 0.1f });
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUpdateResponse() {
		int sz = 5;
		auto = new AutoReBalance(sz);
		auto.reload(new float[] { 0.1f, 0.1f, 0.1f, 0.1f, 0.1f });

		auto.updateResponse(0, 0.1f, true);
		auto.updateResponse(1, 0.2f, false);
		auto.updateResponse(2, 0.3f, true);
		auto.updateResponse(3, 0.4f, false);
		auto.updateResponse(4, 0.5f, true);
		for (int i = 0; i < sz; i++) {
			double a = Double.valueOf(auto.cdd.resp[i].getElement(0)) - Double.valueOf(((i + 1) * 0.1));
			Assert.assertTrue(Math.abs(a) < 0.0001);
			a = Double.valueOf(auto.cdd.fail[i].get()) - Double.valueOf(((i % 2 == 0) ? 0 : 1));
			Assert.assertTrue(Math.abs(a) < 0.0001);
		}
	}

	@Test
	public void testUpdateLoad() {
		int sz = 5;
		auto = new AutoReBalance(sz);
		auto.reload(new float[] { 0.1f, 0.1f, 0.1f, 0.1f, 0.1f });
		auto.updateLoad(new float[] { 10f, 11f, 12f, 13f, 14f });
		for (int i = 0; i < sz; i++) {
			// Assert.assertTrue(auto.cdd.load[i].getElement(0) == 10 + i);
		}
	}

	@Test
	public void testCheckpoint() {
		int sz = 5;
		auto = new AutoReBalance(sz);
		auto.reload(new float[] { 0.1f, 0.1f, 0.1f, 0.1f, 0.1f });
		auto.updateResponse(0, 0.1f, true);
		auto.updateResponse(1, 0.1f, true);
		auto.updateResponse(2, 0.1f, true);
		auto.updateResponse(3, 0.1f, true);
		auto.updateResponse(4, 0.1f, true);
		auto.updateLoad(new float[] { 10, 10, 50, 10, 10 });
		auto.checkpoint();
		Integer[] arr = new Integer[10000];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = auto.sample();
		}
		printSample(arr);
	}

	@Test
	public void testSize() {
		Assert.assertTrue(auto.size() == 5);
	}

	@Test
	public void testReload() {
		auto = new AutoReBalance(5);
		auto.reload(new float[] { 0.1f, 0.1f, 0.1f, 0.1f, 0.1f });
		Integer[] arr = new Integer[10000];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = auto.sample();
		}
		printSample(arr);
	}

	protected Map<Integer, AtomicInteger> printSample(Integer[] arr) {
		HashMap<Integer, AtomicInteger> ret = new HashMap<>();
		for (Integer a : arr) {
			if (!ret.containsKey(a)) {
				ret.put(a, new AtomicInteger(0));
			}
			ret.get(a).incrementAndGet();
		}
		System.out.println(ret);
		return ret;
	}

}
