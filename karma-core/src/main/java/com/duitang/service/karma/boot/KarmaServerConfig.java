package com.duitang.service.karma.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.support.ClusterRegistry;
import com.duitang.service.karma.trace.NoopTraceVisitor;
import com.duitang.service.karma.trace.TraceVisitor;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public class KarmaServerConfig {

	static Logger logger = LoggerFactory.getLogger(KarmaServerConfig.class);

	protected static TraceVisitor simpleVisitor = new NoopTraceVisitor();
	public static ClusterRegistry clusterAware = new ClusterRegistry();
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

		ClusterRegistry aware = KarmaFinders.findClusterRegistry();
		if (aware != null) {
			clusterAware = aware;
		}
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
