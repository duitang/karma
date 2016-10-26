package com.duitang.service.karma.client.impl;

import java.util.Arrays;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.client.BalancePolicy;

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
		cdd.updateByIdx(i, new float[] { resp1, ok1 ? 0.000000001f : 1, -1 });
	}

	@Override
	public void updateLoad(float[] load) {
		Candidates cd = cdd;
		for (int i = 0; i < load.length; i++) {
			cd.updateByIdx(i, new float[] { -1, -1, load[i] });
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
		for (int i = 0; i < ret.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("Node[").append(i).append("] ==> ").append(Arrays.toString(cdd1.choice)).append(" ");
			sb.append("Current[" + minWin + "]: resp=").append(cdd1.resp[i].getMean()).append("s ; failure=")
					.append(cdd1.fail[i].getMean()).append(" ; load=").append(cdd1.load[i]).append(" ; decay=")
					.append(cdd1.decay[i]);
			sb.append(" .+. History[" + moreWin + "]: resp=").append(cdd1.resp[i].getMean()).append("s ; failure=")
					.append(cdd1.fail[i].getMean()).append(" ; load=").append(cdd1.load[i]).append(" ; decay=")
					.append(cdd1.decay[i]);
			ret[i] = sb.append("\n").toString();
		}
		return ret;
	}

}

class Candidates {

	public final static float VERY_TRIVIA = 0.000001f;

	protected float wResp = 0.3f; // weight of response
	// protected double wLoad = 0.15; // weight of Load
	protected float wFail = 0.4f; // weight of Failure

	protected float wRespAvg = 0.1f; // weight of response history
	// protected double wLoadAvg = 0.05; // weight of Load history
	protected float wFailAvg = 0.2f; // weight of Failure history

	int count;
	float[] choice;

	DescriptiveStatistics[] resp; // 0
	DescriptiveStatistics[] fail; // 1
	float[] load; // 2

	DescriptiveStatistics[] respAvg; // 3
	DescriptiveStatistics[] failAvg; // 4
	DescriptiveStatistics[] loadAvg; // 5

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
		resp = new DescriptiveStatistics[count];
		load = new float[count];
		fail = new DescriptiveStatistics[count];

		respAvg = new DescriptiveStatistics[count];
		loadAvg = new DescriptiveStatistics[count];
		failAvg = new DescriptiveStatistics[count];

		for (int i = 0; i < count; i++) {
			resp[i] = new DescriptiveStatistics();
			resp[i].setWindowSize(minWin);
			load[i] = VERY_TRIVIA;
			fail[i] = new DescriptiveStatistics();
			fail[i].setWindowSize(minWin);

			respAvg[i] = new DescriptiveStatistics();
			respAvg[i].setWindowSize(moreWin);
			loadAvg[i] = new DescriptiveStatistics();
			loadAvg[i].setWindowSize(minWin); // special
			failAvg[i] = new DescriptiveStatistics();
			failAvg[i].setWindowSize(moreWin);
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
				fail[idx].addValue(vals[1]);
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

		float l;
		float l2;

		for (int i = 0; i < choice.length; i++) {
			l = load[i];
			float resp_snap = Double.valueOf(resp[i].getMean()).floatValue();
			float load_snap = l > 0 ? l : VERY_TRIVIA;
			float fail_snap = Double.valueOf(fail[i].getMean()).floatValue();

			l2 = Double.valueOf(loadAvg[i].getMean()).floatValue();
			float respAvg_snap = Double.valueOf(respAvg[i].getMean()).floatValue();
			float loadAvg_snap = l2 > 0 ? l2 : VERY_TRIVIA;
			float failAvg_snap = Double.valueOf(failAvg[i].getMean()).floatValue();

			choice[i] = decay[i] * ((wResp * resp_snap * l + wRespAvg * respAvg_snap * l2)
					+ (wFail * fail_snap * l + wFailAvg * failAvg_snap * l2));
			choice[i] = 1f / (1 + choice[i]);

			if (AutoReBalance.log.isDebugEnabled()) {
				AutoReBalance.log.debug("checkpoint => " + choice[i] + ", statistics = "
						+ Arrays.asList(resp_snap, load_snap, fail_snap, respAvg_snap, loadAvg_snap, failAvg_snap));
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
