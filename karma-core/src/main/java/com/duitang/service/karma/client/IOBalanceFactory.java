package com.duitang.service.karma.client;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.support.ClusterRegistry;
import com.duitang.service.karma.support.RPCUrls;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public interface IOBalanceFactory {

	public IOBalance createIOBalance(ClusterRegistry clusterAware, RPCUrls urls) throws KarmaException;

}
