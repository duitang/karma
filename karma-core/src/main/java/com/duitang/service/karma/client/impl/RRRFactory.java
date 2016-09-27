package com.duitang.service.karma.client.impl;

import java.util.List;

import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class RRRFactory implements IOBalanceFactory {

	@Override
	public IOBalance createIOBalance(List<String> urls) {
		return new RoundRobinRouter(urls);
	}

}
