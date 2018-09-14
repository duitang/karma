/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.cluster;

import java.util.Arrays;
import java.util.List;

import com.duitang.service.karma.boot.KarmaFinder;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.server.AsyncRegistryWriter;
import com.duitang.service.karma.support.RPCRegistry;

/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
public class Finder implements KarmaFinder {

	static RPCRegistry registry = new RPCRegistry();

	public <T> T find(Class<T> clazz) {
		return (T) registry;
	}

	/**
	 * should be enabled very early, at least before server bootstrap
	 * 
	 * @param conn
	 * @param urls
	 * @param period
	 * @param count
	 * @param and
	 */
	public static void enableZKRegistry(String conn, List<String> urls, long period, int count, boolean and) {
		ZKClusterWorker ret = ZKClusterWorker.createInstance(conn);
		Finder.registry.addWriters(Arrays.asList((AsyncRegistryWriter) ret.zkSR));
		Finder.registry.addReaders(Arrays.asList((AsyncRegistryReader) ret.lsnr));
		ClusterAwareBalancerFactory fac = new ClusterAwareBalancerFactory(period, count, and);
		fac.setBootstrapURLs(urls);
		Finder.registry.setFactory(fac);
		// no disable support because of already alive service
	}

	public static RPCRegistry getRegistry() {
		return Finder.registry;
	}

}
