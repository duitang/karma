/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.duitang.service.karma.boot.KarmaFinder;
import com.duitang.service.karma.server.AsyncRegistryWriter;

/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
public class Finder implements KarmaFinder {

	static ConcurrentLinkedQueue<AsyncRegistryWriter> registries = new ConcurrentLinkedQueue<>();

	public <T> T find(Class<T> clazz) {
		ArrayList<AsyncRegistryWriter> lst = new ArrayList<>(registries);
		return (T) lst.toArray(new AsyncRegistryWriter[lst.size()]);
	}

	/**
	 * should be enabled very early, at least before server bootstrap
	 * 
	 * @param host
	 * @param port
	 */
	public static void enableZKRegistry(String conn) {
		CuratorClusterWorker ret = CuratorClusterWorker.createInstance(conn);
		Finder.registries.add(ret.zkSR);
		// no disable support because of already alive service
	}

	public static List<AsyncRegistryWriter> getRegistryWriters() {
		ArrayList<AsyncRegistryWriter> ret = new ArrayList<AsyncRegistryWriter>();
		ret.addAll(registries);
		return ret;
	}

}
