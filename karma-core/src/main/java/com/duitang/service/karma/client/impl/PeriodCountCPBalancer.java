/**
 * @author laurence
 * @since 2016年9月30日
 *
 */
package com.duitang.service.karma.client.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.duitang.service.karma.support.RPCNodeHashing;

/**
 * @author laurence
 * @since 2016年9月30日
 *
 */
public class PeriodCountCPBalancer extends TraceableBalancer {

	public final static long PERIOD = 30 * 1000; // in milliseconds, 60s
	public final static int COUNT = 0; // in count
	public final static boolean AND = false; // use and logic

	// default in 60s and no count limit
	protected long period; // in milliseconds, 60s
	protected int count; // in count
	protected boolean and; // use and logic

	// hit if reach count
	protected AtomicInteger watermark = new AtomicInteger(0);

	// hit if now > nextpoint
	protected long nextPoint;

	public PeriodCountCPBalancer(List<String> urls) {
		this(RPCNodeHashing.createFromString(urls), PERIOD, COUNT, AND);
	}

	public PeriodCountCPBalancer(RPCNodeHashing urls) {
		this(urls, PERIOD, COUNT, AND);
	}

	public PeriodCountCPBalancer(List<String> urls, long period, int count, boolean and) {
		this(RPCNodeHashing.createFromString(urls), period, count, and);
	}

	public PeriodCountCPBalancer(RPCNodeHashing urls, long period, int count, boolean and) {
		super(urls);
		this.period = period;
		this.count = count;
		this.and = and;
		nextPoint = System.currentTimeMillis() + period;
	}

	@Override
	boolean hitPoint() {
		boolean hitPeriod = false;
		boolean hitCount = false;
		if (period > 0) {
			hitPeriod = nextPoint < System.currentTimeMillis();
		}
		if (count > 0) {
			int nowCount = watermark.get();
			hitCount = count <= nowCount;
		}

		boolean ret = and ? (hitPeriod && hitCount) : (hitPeriod || hitCount);
		if (ret && hitPeriod) {
			nextPoint = System.currentTimeMillis() + period;
		}
		if (ret && hitCount) {
			watermark.set(0); // force reset, ignore racing
		}
		return ret;
	}

	@Override
	int count1(String token) {
		return watermark.incrementAndGet();
	}

}
