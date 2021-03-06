/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
package com.duitang.service.karma.trace;

import java.net.URISyntaxException;

import com.duitang.service.karma.trace.zipkin.UDPGELFLogger;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
abstract public class ReporterFactory {

	final public static String ZIPKIN = "ZIPKIN";

	public static TracerReporter createReporter(String type, String url) throws URISyntaxException {
		if (ZIPKIN.equals(type)) {
			return ReporterSender.addZipkinSender(url);
		}
		throw new RuntimeException("not found: " + type + ", with url: " + url);
	}

	public static TracerLogger createGELFUDPLogger(String host, int port) {
		return new UDPGELFLogger(host, port);
	}

}
