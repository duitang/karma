/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
package com.duitang.service.karma.server;

import com.duitang.service.karma.KarmaException;

/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
public interface AsyncRegistryWriter {

	/**
	 * register to Service HUP
	 * 
	 * @param rpc
	 */
	void register(RPCService rpc) throws KarmaException;

	/**
	 * unregister Service from HUP
	 * 
	 * @param rpc
	 */
	void unregister(RPCService rpc) throws KarmaException;

	/**
	 * heartbeat for alive report
	 * 
	 * @param rpc
	 */
	void syncPush(RPCService rpc);

}
