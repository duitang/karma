package com.duitang.service.karma.boot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.client.impl.TraceableBalancerFactory;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RPCRegistry;
import com.duitang.service.karma.trace.NoopTraceVisitor;
import com.duitang.service.karma.trace.TraceVisitor;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class KarmaClientConfig {

	static Logger logger = LoggerFactory.getLogger(KarmaClientConfig.class);

	final static public long KARMA_CLIENT_TIMEOUT = 10 * 1000; // 10s

	protected static RPCRegistry clusterAware = new RPCRegistry();
	protected static IOBalanceFactory simpleFactory = new TraceableBalancerFactory(60 * 1000, 0, false);
	protected static TraceVisitor simpleVisitor = new NoopTraceVisitor();

	public static volatile Map<String, IOBalance> balanceRouter = new HashMap<String, IOBalance>();
	public static volatile Map<String, IOBalanceFactory> routerFac = new HashMap<String, IOBalanceFactory>();

	protected static volatile Map<String, TraceVisitor> tracer = new HashMap<String, TraceVisitor>();

	static {
		// register plugin here
		TraceVisitor tr = KarmaFinders.findTraceImpl();
		if (tr != null) {
			simpleVisitor = tr;
		}
		logger.info("using TraceVisitor: " + simpleVisitor.getClass().getName());

		RPCRegistry aware = KarmaFinders.findClusterRegistry();
		if (aware != null) {
			clusterAware = aware;
			simpleFactory = aware.getFactory();
		}
		logger.info("using ClusterAware: " + clusterAware.getInfo());
		logger.info("using IOBalanceFacotry: " + simpleFactory.getClass().getName());
	}

	synchronized public static void bindFactory(String group, IOBalanceFactory fac) {
		Map<String, IOBalanceFactory> m0 = new HashMap<String, IOBalanceFactory>(routerFac);
		m0.put(group, fac);
		routerFac = Collections.unmodifiableMap(m0);
	}

	synchronized public static IOBalance bindBalance(String group, List<String> urls) throws KarmaException {
		IOBalanceFactory fac = getFactory(group);
		RPCNodeHashing u = urls == null ? null : RPCNodeHashing.createFromString(urls);
		IOBalance ret = fac.createIOBalance(clusterAware, u);
		Map<String, IOBalance> m = new HashMap<String, IOBalance>(balanceRouter);
		m.put(group, ret);
		balanceRouter = Collections.unmodifiableMap(m);
		return ret;
	}

	synchronized public static void updateTracer(String group, TraceVisitor v) {
		HashMap<String, TraceVisitor> m = new HashMap<String, TraceVisitor>();
		m.put(group, v);
		tracer = Collections.unmodifiableMap(m);
	}

	public static IOBalance getOrCreateIOBalance(String group, List<String> urls) throws KarmaException {
		IOBalance ret = null;
		if (group == null && urls == null) {
			throw new KarmaException("no group, no urls for create client!");
		}

		if (group != null) { // high priority using grp
			ret = balanceRouter.get(group);
			if (ret == null) {
				ret = bindBalance(group, urls);
			}
		} else { // using urls and no binding
			IOBalanceFactory fac = getFactory(group);
			ret = fac.createIOBalance(clusterAware, RPCNodeHashing.createFromString(urls));
		}
		return ret;
	}

	public static TraceVisitor getTraceVisitor(String group) {
		TraceVisitor ret = tracer.get(group);
		if (ret == null) {
			ret = simpleVisitor;
		}
		return ret;
	}

	static IOBalanceFactory getFactory(String grp) {
		IOBalanceFactory ret = routerFac.get(grp);
		if (ret == null) {
			ret = simpleFactory;
		}
		return ret;
	}

}
