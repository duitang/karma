package com.duitang.service.karma.client.impl;

import java.util.List;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.support.ClusterRegistry;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class RRRFactory implements IOBalanceFactory {

	@Override
	public IOBalance createIOBalance(ClusterRegistry clusterAware, List<String> urls) throws KarmaException {
		NaiveBalancer ret = new NaiveBalancer(urls);
		clusterAware.registerRead(ret);
		return ret;
	}

}
