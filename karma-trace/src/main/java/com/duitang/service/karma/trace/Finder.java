/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
package com.duitang.service.karma.trace;

import java.net.URISyntaxException;

import com.duitang.service.karma.boot.KarmaFinder;
import com.duitang.service.karma.trace.zipkin.ZipkinReporterImpl;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public class Finder implements KarmaFinder {

	static public BaseVisitor visitor = new BaseVisitor();

	static public TracerLogger logger = new TracerLogger() {

		@Override
		public void log(TraceCell tc) {
			// ignore
		}

	};

	@Override
	public <T> T find(Class<T> clazz) {
		return (T) visitor;
	}

	public static void enableZipkin(String grp, String url) throws URISyntaxException {
		visitor.addReporter(grp,
				ReporterFactory.createReporter(com.duitang.service.karma.trace.ReporterFactory.ZIPKIN, url));
	}

	public static void disableZipkin(String grp) {
		visitor.removeReporter(grp);
	}

	public static void enableConsole(boolean enabled) {
		ZipkinReporterImpl.useConsole = enabled;
	}

	public static void enableLogger(String url, int port) {
		logger = ReporterFactory.createLogger(url, port);
	}

}
