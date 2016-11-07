package com.duitang.service.karma.client.impl;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.client.BalancePolicy;
import com.duitang.service.karma.support.NodeDD;

/**
 * DYNAMIC LOAD BALANCE
 * 
 * <pre>
 * 
 * 使用三个维度的指标去猜测该路由的结点
 * 1. 响应时间resp：以500ms为100%做归一化
 * 2. 失败率fail：成功为0.1，失败为1
 * 3. 负载load：以50个连接为100%做归一化
 * 
 * 以负ln求和作为activation函数：
 * E = (-1) * ( wResp * ln(resp) + wRespAvg * ln(respAvg) 
 *            + wLoad * ln(load) + wLoadAvg * ln(loadAvg)
 *            + wFail * ln(fail) + wFailAvg * ln(failAvg)
 *            )
 * 
 * 以activation函数作为各结点被路由的选择概率：
 * P_N1 = E_N1 / (E_N1 + E_N2 + ... + E_Nn)
 * 
 * 
 * 这三个维度的采样，分为两个统计窗口：
 * 1. 小窗：代表最近的微观审计
 * 2. 大窗：代表周期性的宏观审计
 * 
 * To be done:
 * 		增加小窗与大窗的梯度计算，根据梯度及动量变化，来修正activation中的wXXXX
 * 
 * </pre>
 * 
 * @author laurence
 *
 */
public class AutoReBalance implements BalancePolicy {

	static Logger log = LoggerFactory.getLogger(AutoReBalance.class);

	int minWin = 100;
	int moreWin = minWin * 50;

	protected volatile Candidates cdd;

	public AutoReBalance(int nodeCount) {
		cdd = new Candidates(nodeCount, minWin, moreWin);
	}

	@Override
	public void updateResponse(int i, float resp1, boolean ok1) {
		cdd.updateByIdx(i, new float[] { resp1, ok1 ? 0 : 1, -1 });
	}

	@Override
	public void updateLoad(float[] load) {
		Candidates cd = cdd;
		for (int i = 0; i < load.length; i++) {
			cd.updateByIdx(i, new float[] { -1, -1, load[i] > 0 ? load[i] : 0 });
		}
	}

	@Override
	public void checkpoint() {
		cdd.checkpoint();
	}

	/**
	 * multinomial sampling
	 * 
	 * @return
	 */
	@Override
	public int sample() {
		double s = Math.random();
		Candidates cd = cdd;
		for (int i = 0; i < cd.choice.length; i++) {
			if (s < cd.choice[i]) {
				return i;
			}
		}
		return cd.choice.length - 1;
	}

	@Override
	public int size() {
		return cdd.count;
	}

	@Override
	synchronized public void reload(float[] samples) {
		Candidates cdd1 = new Candidates(samples.length, minWin, moreWin);
		float total = 0;
		cdd1.choice = new float[samples.length];
		for (float d : samples) {
			total += d;
		}
		for (int i = 0; i < cdd1.choice.length; i++) {
			cdd1.choice[i] = samples[i] / total;
			if (i > 0) {
				cdd1.choice[i] += cdd1.choice[i - 1];
			}
		}
		cdd = cdd1;
	}

	@Override
	public float[] getWeights() {
		Candidates cdd1 = cdd;
		float[] ret = new float[cdd1.choice.length];
		ret[0] = cdd1.choice[0];
		for (int i = 1; i < ret.length; i++) {
			ret[i] = cdd1.choice[i] - cdd1.choice[i - 1];
		}
		return ret;
	}

	@Override
	public String[] getDebugInfo() {
		Candidates cdd1 = cdd;
		String[] ret = new String[cdd1.count];
		float last = 0;
		for (int i = 0; i < ret.length; i++) {
			NodeDD r = new NodeDD();
			r.setAttr("node_index", i);
			r.setAttr("latest_choice", cdd1.choice[i]);
			r.setAttr("latest_resp", cdd1.resp[i].getMean());
			r.setAttr("history_resp", cdd1.respAvg[i].getMean());
			r.setAttr("latest_fail", cdd1.fail[i]);
			r.setAttr("history_fail", cdd1.failAvg[i].getMean());
			r.setAttr("latest_load", cdd1.load[i]);
			r.setAttr("hisotry_load", cdd1.loadAvg[i].getMean());
			r.setAttr("decay_rate", cdd1.decay[i]);
			r.setAttr("sample_prob", cdd1.choice[i] - last);
			last = cdd1.choice[i];
			ret[i] = r.toString();
		}
		return ret;
	}

}

class Candidates {

	final static float PREC = 10000f;

	protected float wResp = 0.003f; // weight of response
	// protected double wLoad = 0.15; // weight of Load
	protected float wFail = 0.004f; // weight of Failure

	protected float wRespAvg = 0.001f; // weight of response history
	// protected double wLoadAvg = 0.05; // weight of Load history
	protected float wFailAvg = 0.002f; // weight of Failure history

	int count;
	float[] choice;

	SynchronizedDescriptiveStatistics[] resp; // 0
	AtomicLong[] fail; // 1
	float[] load; // 2

	SynchronizedDescriptiveStatistics[] respAvg; // 3
	SynchronizedDescriptiveStatistics[] failAvg; // 4
	SynchronizedDescriptiveStatistics[] loadAvg; // 5

	float[] decay; // 6

	public Candidates(int sz, int minWin, int moreWin) {
		count = sz;
		choice = new float[sz];
		decay = new float[sz];
		// initial for equal probabilities
		choice[0] = 1f / sz;
		for (int i = 1; i < sz; i++) {
			choice[i] += (choice[i - 1] + choice[0]);
			decay[i] = 1; // no change
		}
		resp = new SynchronizedDescriptiveStatistics[count];
		load = new float[count];
		fail = new AtomicLong[count];

		respAvg = new SynchronizedDescriptiveStatistics[count];
		loadAvg = new SynchronizedDescriptiveStatistics[count];
		failAvg = new SynchronizedDescriptiveStatistics[count];

		for (int i = 0; i < count; i++) {
			resp[i] = new SynchronizedDescriptiveStatistics();
			resp[i].setWindowSize(minWin);
			load[i] = 0;
			fail[i] = new AtomicLong(0);

			respAvg[i] = new SynchronizedDescriptiveStatistics();
			respAvg[i].setWindowSize(moreWin);
			loadAvg[i] = new SynchronizedDescriptiveStatistics();
			loadAvg[i].setWindowSize(minWin); // special
			loadAvg[i].addValue(0);
			failAvg[i] = new SynchronizedDescriptiveStatistics();
			failAvg[i].setWindowSize(minWin);
			failAvg[i].addValue(0);
		}
		AutoReBalance.log.info("initialized load balance for nodes = " + count);
	}

	public void updateByIdx(int idx, float[] vals) {
		if (idx < count) {
			if (vals[0] > 0) {
				resp[idx].addValue(vals[0]);
				respAvg[idx].addValue(vals[0]);
			}

			if (vals[1] > 0) {
				fail[idx].addAndGet(Float.valueOf(vals[1]).longValue());
				failAvg[idx].addValue(vals[1]);
			}

			if (vals[2] > 0) {
				load[idx] = vals[2];
				loadAvg[idx].addValue(vals[2]);
			}
		}
	}

	public void checkpoint() {
		float total = 0f;

		float l; // latest load
		float l2; // average load
		float r; // latest response
		float r2; // average response
		float f; // latest fail
		float f2; // average fail

		float fa_total = 0;
		float fa[] = new float[choice.length];
		float re_total = 0;
		float re[] = new float[choice.length];
		float fa_total2 = 0;
		float fa2[] = new float[choice.length];
		float re_total2 = 0;
		float re2[] = new float[choice.length];

		// calculate all total values
		for (int i = 0; i < choice.length; i++) {
			fa[i] = fail[i].getAndSet(0);
			fa[i] += 0.0001; // far from zero
			fa_total += fa[i];

			fa2[i] = Double.valueOf(failAvg[i].getMean()).floatValue();
			fa2[i] += 0.0001; // far from zero
			fa_total2 += fa2[i];

			re[i] = Double.valueOf(resp[i].getMean()).floatValue();
			re[i] += 0.0001; // far from zero
			re_total += re[i];

			re2[i] = Double.valueOf(respAvg[i].getMean()).floatValue();
			re2[i] += 0.0001; // far from zero
			re_total2 += re2[i];
		}

		for (int i = 0; i < choice.length; i++) {
			// latest load level > 1
			l = ((Number) Math.log(Float.valueOf(load[i]).doubleValue() + Math.E)).floatValue();
			// average load level > 1
			l2 = ((Number) Math.log(loadAvg[i].getMean() + Math.E)).floatValue();

			// latest response energy rate (0,1)
			r = (re[i] / re_total);
			// average response energy rate (0,1)
			r2 = (re2[i] / re_total2);

			// latest fail level > 1
			f = Double.valueOf(Math.log(100d * (fa[i] / fa_total) + Math.E)).floatValue();
			// average fail level > 1
			f2 = Double.valueOf(Math.log(100d * (fa2[i] / fa_total2) + Math.E)).floatValue();

			choice[i] = decay[i] * ((PREC * wResp * r * l * wFail * f + PREC * wRespAvg * r2 * l2 * wFailAvg * f2));
			// higher energy => smaller probability 
			choice[i] = 1f / (1 + choice[i]);
			// SOFTMAX
			choice[i] = Double.valueOf(Math.pow(Math.E, choice[i])).floatValue();

			if (AutoReBalance.log.isDebugEnabled()) {
				AutoReBalance.log
						.debug("checkpoint => " + choice[i] + ", statistics = " + Arrays.asList(r, l, f, r2, l2, f2));
			}
			total += choice[i];
		}
		for (int i = 0; i < choice.length; i++) {
			choice[i] = choice[i] / total;
			if (i > 0) {
				choice[i] += choice[i - 1];
			}
		}

		if (AutoReBalance.log.isDebugEnabled()) {
			AutoReBalance.log.debug("checkpoint choice = " + Arrays.toString(choice));
		}
	}

}
