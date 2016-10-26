package com.duitang.service.karma.client.impl;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FixedPolicyTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() {
		float[] loads = new float[] { 0.4f, 0.8f, 0.8f };
		FixedPolicy p = new FixedPolicy(loads);

		Assert.assertTrue(p.size() == loads.length);
		float[] ss = p.getWeights();
		Assert.assertTrue(Math.abs(Double.valueOf(ss[0] - 0.2d)) < 0.001);
		Assert.assertTrue(Math.abs(Double.valueOf(ss[1] - 0.4d)) < 0.001);
		Assert.assertTrue(Math.abs(Double.valueOf(ss[2] - 0.4d)) < 0.001);

		p.checkpoint();
		Assert.assertTrue(p.size() == loads.length);

		ss = p.getWeights();
		Assert.assertTrue(Math.abs(Double.valueOf(ss[0] - 0.2d)) < 0.001);
		Assert.assertTrue(Math.abs(Double.valueOf(ss[1] - 0.4d)) < 0.001);
		Assert.assertTrue(Math.abs(Double.valueOf(ss[2] - 0.4d)) < 0.001);

		int[] ct = new int[3];
		for (int i = 0; i < 10000; i++) {
			ct[p.sample()]++;
		}
		System.out.println(Arrays.toString(ct));
		System.out.println(Arrays.toString(p.getDebugInfo()));

	}

}
