package com.duitang.service.karma.client;

import java.util.List;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public interface IOBalanceFactory {

	public IOBalance createIOBalance(List<String> urls);

}
