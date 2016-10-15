package com.duitang.service.karma.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.support.RPCRegistry;
import com.duitang.service.karma.trace.NoopTraceVisitor;
import com.duitang.service.karma.trace.TraceVisitor;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public class KarmaServerConfig {

	final public static long KARMA_SERVER_SHUTDOWN_TIMEOUT = 10 * 1000L;
	final static Logger logger = LoggerFactory.getLogger(KarmaServerConfig.class);

	static TraceVisitor simpleVisitor = new NoopTraceVisitor();
	final public static RPCRegistry clusterAware;
	public static volatile TraceVisitor tracer = simpleVisitor;

	public static volatile int host;
	public static volatile int port;

	static {
		// register plugin here
		TraceVisitor tr = KarmaFinders.findTraceImpl();
		if (tr != null) {
			simpleVisitor = tr;
			tracer = simpleVisitor;
		}
		logger.info("using TraceVisitor: " + simpleVisitor.getClass().getName());

		RPCRegistry aware = KarmaFinders.findClusterRegistry();
		clusterAware = aware == null ? new RPCRegistry() : aware;
		logger.info("using ClusterAware: " + clusterAware.getInfo());
	}

	synchronized public static void useTracer(TraceVisitor v) {
		tracer = v;
	}

	synchronized public static void updateHostInfo(int h, int p) {
		host = h;
		KarmaServerConfig.port = p;
	}

}
