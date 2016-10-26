package com.duitang.service.karma.client.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TestWithPolicy {

	final static String[] caption = { "fast", "common", "slow" };

	final static double[][] respSample = new double[][] {
			{ 0.060d, 0.001d }, // 60ms, fast
			{ 0.200d, 0.01d }, // 200ms, common
			{ 0.500d, 0.001d }// 500ms, slow
	};

	final static double[] opOKSample = new double[] {
			0.9999d, // ten-thousandth
			0.999d, // thousandth
			0.05d, // 5%
	};

	final static double[][] loadSample = new double[][] {
			{ 20d, 0.01d }, // lightweight
			{ 40d, 0.01d }, // normal
			{ 60d, 0.01d }// overload
	};

	public static void main(String[] args) {
		Logger root = (Logger) LoggerFactory.getLogger(AutoReBalance.class);
		root.setLevel(Level.DEBUG);

		test(5, 1000, new int[] { 0, 1, 2, 0, 1 });
		test(5, 1000, new int[] { 0, 2, 2, 2, 1 });
		test(5, 1000, new int[] { 0, 1, 2, 1, 1 });
	}

	static void test(int loop, int count, int[] profiles) {
		AutoReBalance lb = new AutoReBalance(profiles.length);
		float[] init = new float[profiles.length];
		for (int i = 0; i < init.length; i++) {
			init[i] = 1f / init.length;
		}
		lb.reload(init);
		for (int i = 0; i < loop; i++) { // turn
			Integer[] samples = new Integer[10000];
			simulation(profiles, count, lb);
			for (int j = 0; j < samples.length; j++) {
				int pos = lb.sample();
				samples[j] = pos;
			}
			Map<Integer, AtomicInteger> ret = printSample(samples);
			String[] title = new String[profiles.length];
			for (int j = 0; j < profiles.length; j++) {
				title[j] = caption[profiles[j]];
			}
			System.out.println(Arrays.toString(title) + " ==> " + ret);
		}
	}

	static protected Map<Integer, AtomicInteger> printSample(Integer[] arr) {
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

	static void simulation(int[] profile, int count, AutoReBalance lb) {
		Random r = new Random();
		NormalDistribution[] profiles = new NormalDistribution[profile.length];

		for (int i = 0; i < profile.length; i++) {
			profiles[i] = new NormalDistribution(respSample[profile[i]][0], respSample[profile[i]][1]);
		}

		for (int k = 0; k < 20; k++) { // should give times for stable

			float[] load = new float[lb.size()];
			for (int i = 0; i < count; i++) { // how many records
				int j = lb.sample(); // choice next node
				load[j]++;
				// generate node's performance
				float resp = Double.valueOf(profiles[j].sample()).floatValue();
				// generate if node's failure
				boolean ok = r.nextDouble() < opOKSample[profile[j]] ? true : false;
				lb.updateResponse(j, resp, ok); // after every RPC_CALL finished
			}

			// at checkpoint
			lb.updateLoad(load);
			lb.checkpoint(); // checkpoint for choice probability update
		}
	}

}
