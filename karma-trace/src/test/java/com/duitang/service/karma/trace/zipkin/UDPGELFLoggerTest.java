package com.duitang.service.karma.trace.zipkin;

import com.duitang.service.karma.trace.ReporterFactory;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TracerLogger;

import com.github.pukkaone.gelf.logback.GelfAppender;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by water on 9/28/16.
 */
public class UDPGELFLoggerTest {

	@Test
	public void testLog() throws Exception {
		TracerLogger logger = ReporterFactory.createLogger("61.152.115.82", 30011);

		logger.log(null, new TraceCell(false, "", 1));
	}


	@Test
	public void testConfigurableAppender() {

		GelfAppender gelfAppender = new GelfAppender();
		gelfAppender.setName("gelfudp");
		gelfAppender.setGraylogHost("61.152.115.82");
		gelfAppender.setGraylogPort(30011);
		gelfAppender.setOriginHost("myKarmaZipkin");
		gelfAppender.setLevelIncluded(true);
		gelfAppender.setLocationIncluded(true);
		gelfAppender.setLoggerIncluded(true);
		gelfAppender.setMarkerIncluded(true);
		gelfAppender.setMdcIncluded(true);
		gelfAppender.setThreadIncluded(false);
		gelfAppender.setFacility("gelf-java");
		gelfAppender.addAdditionalField("application=karma");
		gelfAppender.addAdditionalField("environment=dev");

		TracerLogger logger = ReporterFactory.createConfigurableLogger(gelfAppender);
		logger.log(null, new TraceCell(false, "", 1));
	}
}