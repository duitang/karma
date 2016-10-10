/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
package com.duitang.service.karma.boot;

import java.util.HashMap;
import java.util.Map;

import com.duitang.service.karma.server.CoreEnhanced;
import com.duitang.service.karma.support.RPCRegistry;
import com.duitang.service.karma.trace.TraceVisitor;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public class KarmaFinders {

	final static Map<String, KarmaFinder> cache = new HashMap<String, KarmaFinder>();
	final static String KARMA_TRACE = "com.duitang.service.karma.trace.Finder";
	final static String KARMA_HTTP = "com.duitang.service.karma.http.Finder";
	final static String KARMA_CLUSTER = "com.duitang.service.karma.cluster.Finder";

	synchronized static private <T> T findKarmaImpl(Class<T> clazz, String fullName) {
		if (!cache.containsKey(fullName)) {
			try {
				Class entry = Class.forName(fullName);
				if (KarmaFinder.class.isAssignableFrom(entry)) {
					KarmaFinder f = (KarmaFinder) entry.newInstance();
					cache.put(fullName, f);
				}
			} catch (Exception e) {
				// ignore
			}
		}
		T ret = null;
		if (cache.containsKey(fullName)) {
			ret = cache.get(fullName).find(clazz);
		}
		return ret;
	}

	public static TraceVisitor findTraceImpl() {
		return findKarmaImpl(TraceVisitor.class, KARMA_TRACE);
	}

	public static CoreEnhanced findProtocolSupport() {
		return findKarmaImpl(CoreEnhanced.class, KARMA_HTTP);
	}

	public static RPCRegistry findClusterRegistry() {
		return findKarmaImpl(RPCRegistry.class, KARMA_CLUSTER);
	}

}
