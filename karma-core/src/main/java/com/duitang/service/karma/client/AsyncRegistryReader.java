/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.client;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.support.RegistryInfo;

/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
public interface AsyncRegistryReader {

	/**
	 * register for dynamic config observer
	 * 
	 * @param balancer IO Channel Load Balance
	 * @throws KarmaException default exception
	 */
	void register(IOBalance balancer) throws KarmaException;

	/**
	 * deregister for dynamic config observer
	 * @param balancer IO Channel Load Balance
	 * @throws KarmaException default exception
	 */
	void unregister(IOBalance balancer) throws KarmaException;

	/**
	 * IOBalance pull config from dynamic source
	 * 
	 * @return from cluster register
	 */
	RegistryInfo syncPull();

}
