package com.duitang.service.karma.client;

import java.util.List;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.support.ClusterRegistry;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public interface IOBalanceFactory {

	public IOBalance createIOBalance(ClusterRegistry clusterAware, List<String> urls) throws KarmaException;

}
