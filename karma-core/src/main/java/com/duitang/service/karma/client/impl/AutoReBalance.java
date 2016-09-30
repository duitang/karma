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
	public void updateResponse(int i, double resp1, boolean ok1) {
		cdd.updateByIdx(i, new double[] { resp1, ok1 ? 0.000000001 : 1, -1 });
	}

	@Override
	public void updateLoad(double[] load) {
		Candidates cd = cdd;
		for (int i = 0; i < load.length; i++) {
			cd.updateByIdx(i, new double[] { -1, -1, load[i] });
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
	synchronized public void reload(double[] samples) {
		Candidates cdd1 = new Candidates(samples.length, minWin, moreWin);
		double total = 0;
		cdd1.choice = new double[samples.length];
		for (double d : samples) {
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
	public double[] getWeights() {
		return cdd.choice;
	}

	@Override
	public String[] getStats() {
		Candidates cdd1 = cdd;
		String[] ret = new String[cdd1.count];
		for (int i = 0; i < ret.length; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("Current[" + minWin + "]: resp=").append(cdd1.resp[i].getMean()).append("s ; failure=")
					.append(cdd1.fail[i].getMean()).append(" ; load=").append(cdd1.load[i]);
			sb.append(". History[" + moreWin + "]: resp=").append(cdd1.resp[i].getMean()).append("s ; failure=")
					.append(cdd1.fail[i].getMean()).append(" ; load=").append(cdd1.load[i]);
			ret[i] = sb.toString();
		}
		return ret;
	}

}

class Candidates {

	public final static double VERY_TRIVIA = 0.000001d;

	protected double wResp = 0.3; // weight of response
	// protected double wLoad = 0.15; // weight of Load
	protected double wFail = 0.4; // weight of Failure

	protected double wRespAvg = 0.1; // weight of response history
	// protected double wLoadAvg = 0.05; // weight of Load history
	protected double wFailAvg = 0.2; // weight of Failure history

	int count;
	double[] choice;

	DescriptiveStatistics[] resp; // 0
	DescriptiveStatistics[] fail; // 1
	double[] load; // 2

	DescriptiveStatistics[] respAvg; // 3
	DescriptiveStatistics[] failAvg; // 4
	DescriptiveStatistics[] loadAvg; // 5

	public Candidates(int sz, int minWin, int moreWin) {
		count = sz;
		choice = new double[sz];
		// initial for equal probabilities
		choice[0] = 1d / sz;
		for (int i = 1; i < sz; i++) {
			choice[i] += choice[i - 1];
		}
		resp = new DescriptiveStatistics[count];
		load = new double[count];
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

	public void updateByIdx(int idx, double[] vals) {
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
		double total = 0d;

		double l;
		double l2;

		for (int i = 0; i < choice.length; i++) {
			l = load[i];
			double resp_snap = resp[i].getMean();
			double load_snap = l > 0 ? l : VERY_TRIVIA;
			double fail_snap = fail[i].getMean();

			l2 = loadAvg[i].getMean();
			double respAvg_snap = respAvg[i].getMean();
			double loadAvg_snap = l2 > 0 ? l2 : VERY_TRIVIA;
			double failAvg_snap = failAvg[i].getMean();

			choice[i] = (wResp * resp_snap * l + wRespAvg * respAvg_snap * l2)
					+ (wFail * fail_snap * l + wFailAvg * failAvg_snap * l2);
			choice[i] = 1d / (1 + choice[i]);

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
