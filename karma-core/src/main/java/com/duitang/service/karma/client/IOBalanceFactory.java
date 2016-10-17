package com.duitang.service.karma.client;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RPCRegistry;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public interface IOBalanceFactory {

	public IOBalance createIOBalance(RPCRegistry clusterAware, RPCNodeHashing urls) throws KarmaException;

}
