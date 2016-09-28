/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
package com.duitang.service.karma.trace;

import java.net.URISyntaxException;

import com.duitang.service.karma.trace.zipkin.UDPGELFLogger;
import com.duitang.service.karma.trace.zipkin.ZipkinReporterImpl;

import com.github.pukkaone.gelf.logback.GelfAppender;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
abstract public class ReporterFactory {

	final public static String ZIPKIN = "ZIPKIN";

	public static TracerReporter createReporter(String type, String url) throws URISyntaxException {
		if (ZIPKIN.equals(type)) {
			return new ZipkinReporterImpl(url);
		}
		throw new RuntimeException("not found: " + type + ", with url: " + url);
	}

	public static TracerLogger createLogger(String host, int port) {
		return new UDPGELFLogger(host, port);
	}

	public static TracerLogger createConfigurableLogger(GelfAppender appender) {
		return new UDPGELFLogger(appender);
	}

}
