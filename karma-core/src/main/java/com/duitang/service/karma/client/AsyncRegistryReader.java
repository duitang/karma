/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.client;

import com.duitang.service.karma.KarmaException;

/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
public interface AsyncRegistryReader {

	/**
	 * register for dynamic config observer
	 * 
	 * @param balancer
	 */
	void register(IOBalance balancer) throws KarmaException;

	/**
	 * deregister for dynamic config observer
	 */
	void unregister(IOBalance balancer) throws KarmaException;

	/**
	 * IOBalance pull config from dynamic source
	 * 
	 * @return
	 */
	RegistryInfo syncPull();

}
