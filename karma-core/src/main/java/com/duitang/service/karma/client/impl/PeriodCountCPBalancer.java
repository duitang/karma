/**
 * @author laurence
 * @since 2016年9月30日
 *
 */
package com.duitang.service.karma.client.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author laurence
 * @since 2016年9月30日
 *
 */
public class PeriodCountCPBalancer extends TraceableBalancer {

	// default in 60s and no count limit
	protected long period = 60 * 1000; // in milliseconds, 60s
	protected int count = 0; // in count
	protected boolean and = false; // use and logic

	// hit if reach count
	protected AtomicInteger watermark = new AtomicInteger(0);

	// hit if now > nextpoint
	protected long nextPoint = Long.MAX_VALUE;

	public PeriodCountCPBalancer(List<String> urls) {
		super(urls);
	}

	public PeriodCountCPBalancer(List<String> urls, long period, int count, boolean and) {
		super(urls);
		this.period = period;
		this.count = count;
		this.and = and;
	}

	@Override
	boolean hitPoint() {
		boolean hitPeriod = false;
		boolean hitCount = false;
		if (period > 0) {
			hitPeriod = nextPoint < System.currentTimeMillis();
		}
		if (count > 0) {
			// hitPoint increase 1 count every invoke which enabled count
			int nowCount = watermark.incrementAndGet();
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

}
