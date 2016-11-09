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
	public void updateLoad(int i, float load) {
		if (load <= 0) {
			return;
		}
		Candidates cd = cdd;
		cd.updateByIdx(i, new float[] { -1, -1, load });
	}

	@Override
	public void checkpoint() {
		cdd.checkpoint();
	}

	@Override
	public float[] getLoads() {
		float[] ret = new float[cdd.load.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = Long.valueOf(cdd.load[i].get()).floatValue();
		}
		return ret;
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
			r.setAttr("latest_fail", cdd1.failSnap[i]);
			r.setAttr("history_fail", cdd1.failAvg[i].getMean());
			r.setAttr("latest_load", cdd1.loadSnap[i]);
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

	final static float TRIFLE = 0.00001f;

	final static float BASE1 = 0.1f;
	final static float BASE2 = 0.3f;
	final static float BASE3 = 0.6f;

	final static float BALANCE_RATE = 0.1f;

	float[] failSnap;
	float[] loadSnap;

	int count;
	float[] choice;

	SynchronizedDescriptiveStatistics[] resp; // 0
	AtomicLong[] fail; // 1
	AtomicLong[] load; // 2

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
		load = new AtomicLong[count];
		fail = new AtomicLong[count];
		failSnap = new float[count];
		loadSnap = new float[count];

		respAvg = new SynchronizedDescriptiveStatistics[count];
		loadAvg = new SynchronizedDescriptiveStatistics[count];
		failAvg = new SynchronizedDescriptiveStatistics[count];

		for (int i = 0; i < count; i++) {
			resp[i] = new SynchronizedDescriptiveStatistics();
			resp[i].setWindowSize(minWin);
			resp[i].addValue(0);
			load[i] = new AtomicLong(0);
			fail[i] = new AtomicLong(0);

			respAvg[i] = new SynchronizedDescriptiveStatistics();
			respAvg[i].setWindowSize(moreWin);
			respAvg[i].addValue(0);
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
				load[idx].addAndGet(Float.valueOf(vals[2]).longValue());
				loadAvg[idx].addValue(vals[2]);
			}
		}
	}

	public void checkpoint() {
		float total = 0f;
		float l[] = new float[choice.length]; // latest load
		float l2[] = new float[choice.length]; // average load
		float r[] = new float[choice.length]; // latest response
		float r2[] = new float[choice.length]; // average response
		float f[] = new float[choice.length]; // latest fail
		float f2[] = new float[choice.length]; // average fail
		float valid[] = new float[choice.length]; // valid energy
		float error[] = new float[choice.length]; // error energy
		float brate[] = new float[choice.length]; // latest bad rate

		// load energy
		float rank1[] = new float[choice.length];
		// error energy
		float rank2[] = new float[choice.length];
		// error density energy
		float rank3[] = new float[choice.length];

		float least = BALANCE_RATE / choice.length;

		float r1_total = 0f;
		float r2_total = 0f;
		float r3_total = 0f;

		// calculate all total values
		for (int i = 0; i < choice.length; i++) {
			f[i] = fail[i].getAndSet(0);
			f2[i] = Double.valueOf(failAvg[i].getSum()).floatValue();
			l[i] = load[i].getAndSet(0);
			l2[i] = Double.valueOf(loadAvg[i].getMean()).floatValue();
			r[i] = Double.valueOf(resp[i].getMean()).floatValue();
			r2[i] = Double.valueOf(respAvg[i].getMean()).floatValue();

			loadSnap[i] = l[i];
			failSnap[i] = f[i];

			// min(valid[i]) = 1
			valid[i] = Math.max(1, l[i] * r[i] * 1000) + 1;
			// valid[i] = Double.valueOf(Math.log10(valid[i])).floatValue() + 1;

			// min(error[i]) = 1
			error[i] = Math.max(0, f[i]);
			error[i] = Double.valueOf(Math.log10(error[i] + 1)).floatValue() + 1;

			// min(brate[i]) = 1
			brate[i] = 100f * f[i] / (l[i] + TRIFLE);
			// max(brate[i]) = 1000
			brate[i] = Math.min(100, brate[i]);
			// min(brate[i]) = 1
			brate[i] = Math.max(1, brate[i]);
			// brate[i] = Double.valueOf(Math.log(brate[i] + 1)).floatValue() +
			// 1;

			// error density layer
			rank3[i] = brate[i];

			// error layer
			rank2[i] = Double.valueOf(error[i]).floatValue() + 1;

			// load layer
			rank1[i] = Double.valueOf(Math.log10(valid[i])).floatValue() + 1;
			// decay for lost nodes
			rank1[i] *= decay[i];

			// higher energy => smaller probability
			rank1[i] = 1 / (1 + rank1[i]);
			rank2[i] = 1 / (1 + rank2[i]);
			rank3[i] = 1 / (1 + rank3[i]);

			r1_total += rank1[i];
			r2_total += rank2[i];
			r3_total += rank3[i];

		}

		// imbuto model
		for (int i = 0; i < choice.length; i++) {
			// choice[i] = (1 / TRIFLE) * (rank1[i] / r1_total) * (rank2[i] /
			// r2_total) * (rank3[i] / r3_total);
			choice[i] = BASE1 * (rank1[i] / r1_total) + BASE2 * (rank2[i] / r2_total) + BASE3 * (rank3[i] / r3_total);
			total += choice[i];
		}

		System.out.println(Arrays.toString(error));

		for (int i = 0; i < choice.length; i++) {
			// notice: least to average
			choice[i] = choice[i] / total;
			choice[i] = (least + choice[i]) / (1f + BALANCE_RATE);
			if (i > 0) {
				choice[i] += choice[i - 1];
			}
			if (AutoReBalance.log.isDebugEnabled()) {
				AutoReBalance.log.debug("entropy => " + Math.round(100 * choice[i]) + ", statistics = "
						+ Arrays.asList(r[i], l[i], f[i], valid[i], error[i], brate[i], rank1[i], rank2[i], rank3[i]));
			}
		}

		if (AutoReBalance.log.isDebugEnabled()) {
			AutoReBalance.log.debug("checkpoint choice = " + Arrays.toString(choice));
		}
	}

}
