/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.trace.zipkin;

import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TracerLogger;

import com.github.pukkaone.gelf.logback.GelfAppender;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class UDPGELFLogger implements TracerLogger {

	private Logger logger = (Logger) LoggerFactory.getLogger(UDPGELFLogger.class);

	/**
	 *
	 *
	 * @param host graylog host. such as 192.168.0.1. support udp default.
	 * @param port graylog port.
	 */
	public UDPGELFLogger(String host, int port) {
		initGELFLogger(host, port);
	}

	/**
	 * configurable appender
	 * @param gelfAppender
	 */
	public UDPGELFLogger(GelfAppender gelfAppender) {
		intiGELFLoggerWithConfig(gelfAppender);
	}

	private void intiGELFLoggerWithConfig(GelfAppender gelfAppender) {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		gelfAppender.setContext(lc);
		gelfAppender.start();

		logger.addAppender(gelfAppender);
		logger.setLevel(Level.INFO);
		logger.setAdditive(true);
	}

	private void initGELFLogger(String host, int port) {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		GelfAppender gelfAppender = new GelfAppender();
		gelfAppender.setName("gelfudp");
		gelfAppender.setGraylogHost(host);
		gelfAppender.setGraylogPort(port);
		gelfAppender.setOriginHost("myKarmaZipkin");
		gelfAppender.setLevelIncluded(true);
		gelfAppender.setLocationIncluded(true);
		gelfAppender.setLoggerIncluded(true);
		gelfAppender.setMarkerIncluded(true);
		gelfAppender.setMdcIncluded(true);
		gelfAppender.setThreadIncluded(false);
		gelfAppender.setFacility("gelf-java");
		gelfAppender.addAdditionalField("application=karma");
		gelfAppender.addAdditionalField("environment=medishare");
		gelfAppender.setContext(lc);
		gelfAppender.start();

		logger.addAppender(gelfAppender);
		logger.setLevel(Level.INFO);
		logger.setAdditive(false);
	}

	@Override
	public void log(TraceCell tc) {
		//todo how to use TraceCell
		logger.info(tc.toString());

	}

}
