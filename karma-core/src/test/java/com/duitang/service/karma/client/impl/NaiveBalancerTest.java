package com.duitang.service.karma.client.impl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NaiveBalancerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		List<String> urls = Arrays.asList("aa:111", "bb:222", "cc:333");
		NaiveBalancer nb = new NaiveBalancer(urls);

		int[] ct = new int[urls.size()];
		for (int i = 0; i < 10000; i++) {
			String u = nb.next(null);
			ct[urls.indexOf(u)]++;
		}
		System.out.println(Arrays.toString(ct));

		urls = Arrays.asList("aa:111", "bb:222");
		nb.setNodes(urls);
		ct = new int[urls.size()];
		for (int i = 0; i < 10000; i++) {
			String u = nb.next(null);
			ct[urls.indexOf(u)]++;
		}
		System.out.println(Arrays.toString(ct));

		LinkedHashMap<String, Double> nds = new LinkedHashMap<>();
		nds.put("aa:111", 0.2d);
		nds.put("bb:222", 0.4d);
		nds.put("cc:333", 0.4d);
		nb.setNodesWithWeights(nds);
		ct = new int[nds.size()];
		for (int i = 0; i < 10000; i++) {
			String u = nb.next(null);
			ct[nb.urls.getURLs().indexOf(u)]++;
		}
		System.out.println(Arrays.toString(ct));

	}

}
