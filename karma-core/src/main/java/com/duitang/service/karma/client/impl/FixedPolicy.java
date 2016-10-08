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

	public FixedPolicy(double[] choice) {
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
	public void updateResponse(int i, double resp1, boolean ok1) {
		// ignore
	}

	@Override
	public void updateLoad(double[] load) {
		// ignore
	}

	@Override
	public void reload(double[] samples) {
		SimplePointer ret = new SimplePointer();
		ret.choice = new double[samples.length];
		double total = 0;
		for (double d : samples) {
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
	public double[] getWeights() {
		SimplePointer p = sp;
		double[] ret = new double[p.choice.length];
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

}

class SimplePointer {

	double[] choice;

}
