/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.trace.zipkin;

import com.duitang.service.karma.trace.FormatTraceCellVisitor;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TracerLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	static ObjectMapper mapper = new ObjectMapper();
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
	public void log(FormatTraceCellVisitor visitor, TraceCell tc) {
		if (visitor == null) {  //如果不传visitor,即采用默认的方式,直接整个对象转成json.
			try {
				logger.info(mapper.writeValueAsString(tc));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} else {
			logger.info(visitor.visit(tc));
		}

	}

}
