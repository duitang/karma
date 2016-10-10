package com.duitang.service.karma.client.impl;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.support.RPCRegistry;
import com.duitang.service.karma.support.RPCNodeHashing;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class RRRFactory implements IOBalanceFactory {

	@Override
	public IOBalance createIOBalance(RPCRegistry clusterAware, RPCNodeHashing urls) throws KarmaException {
		NaiveBalancer ret = new NaiveBalancer(urls);
		clusterAware.registerRead(ret);
		return ret;
	}

}
