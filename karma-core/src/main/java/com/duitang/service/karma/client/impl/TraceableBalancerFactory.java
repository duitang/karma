package com.duitang.service.karma.client.impl;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.support.ClusterRegistry;
import com.duitang.service.karma.support.RPCUrls;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class TraceableBalancerFactory implements IOBalanceFactory {

	protected long period;
	protected int count;
	protected boolean and;

	public TraceableBalancerFactory(long period, int count, boolean and) {
		this.period = period;
		this.count = count;
		this.and = and;
	}

	@Override
	public IOBalance createIOBalance(ClusterRegistry clusterAware, RPCUrls urls) throws KarmaException {
		// hit = 60s , no count need, using or
		PeriodCountCPBalancer ret = new PeriodCountCPBalancer(urls, period, count, and);
		clusterAware.registerRead(ret);
		return ret;
	}

}
