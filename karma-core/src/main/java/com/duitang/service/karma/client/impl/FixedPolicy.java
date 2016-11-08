/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
package com.duitang.service.karma.client.impl;

import java.util.Arrays;

import com.duitang.service.karma.client.BalancePolicy;

/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
public class FixedPolicy implements BalancePolicy {

	protected SimplePointer sp;

	public FixedPolicy(float[] choice) {
		reload(choice);
	}

	@Override
	public void checkpoint() {
		// ignore
	}

	@Override
	public int sample() {
		SimplePointer p = sp;
		double s = Math.random();
		for (int i = 0; i < p.choice.length; i++) {
			if (s < p.choice[i]) {
				return i;
			}
		}
		return p.choice.length - 1;
	}

	@Override
	public int size() {
		return sp.choice.length;
	}

	@Override
	public void updateResponse(int i, float resp1, boolean ok1) {
		// ignore
	}

	@Override
	public void updateLoad(int i, float load) {
		// ignore
	}

	@Override
	public void reload(float[] samples) {
		SimplePointer ret = new SimplePointer();
		ret.choice = new float[samples.length];
		float total = 0;
		for (float d : samples) {
			total += d;
		}
		for (int i = 0; i < ret.choice.length; i++) {
			ret.choice[i] = samples[i] / total;
			if (i > 0) {
				ret.choice[i] += ret.choice[i - 1];
			}
		}
		sp = ret;
	}

	@Override
	public float[] getWeights() {
		SimplePointer p = sp;
		float[] ret = new float[p.choice.length];
		ret[0] = p.choice[0];
		for (int i = 1; i < ret.length; i++) {
			ret[i] = p.choice[i] - p.choice[i - 1];
		}
		return ret;
	}

	@Override
	public String[] getDebugInfo() {
		SimplePointer p = sp;
		return new String[] { "samples=" + Arrays.toString(p.choice) };
	}

	@Override
	public float[] getLoads() {
		return new float[sp.choice.length];
	}

}

class SimplePointer {

	float[] choice;

}
