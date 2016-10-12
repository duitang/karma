package com.duitang.service.karma.cluster;

import java.util.List;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.client.impl.PeriodCountCPBalancer;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RPCRegistry;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class ClusterAwareBalancerFactory implements IOBalanceFactory {

	protected long period;
	protected int count;
	protected boolean and;
	protected RPCNodeHashing hashing = null;

	public ClusterAwareBalancerFactory() {
		this(PeriodCountCPBalancer.PERIOD, PeriodCountCPBalancer.COUNT, false);
	}

	public ClusterAwareBalancerFactory(long period, int count, boolean and) {
		this.period = period;
		this.count = count;
		this.and = and;
	}

	@Override
	public IOBalance createIOBalance(RPCRegistry clusterAware, RPCNodeHashing urls) throws KarmaException {
		// hit = 60s , no count need, using or
		urls = urls == null ? hashing : urls;
		if (urls == null) {
			throw new KarmaException("Both Parameter URLs and Bootstrap URLs is null!");
		}
		PeriodCountCPBalancer ret = new PeriodCountCPBalancer(urls, period, count, and);
		clusterAware.registerRead(ret);
		return ret;
	}

	public void setBootstrapURLs(List<String> urls) {
		setBootstrapURLs(RPCNodeHashing.createFromString(urls));
	}

	public void setBootstrapURLs(RPCNodeHashing urls) {
		this.hashing = urls;
	}

}
