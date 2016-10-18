package com.duitang.service.karma.trace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class NoopTraceVisitorTest {

	@Before
	public void setUp() throws Exception {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		root.setAdditive(false);
		Logger logger = (Logger) LoggerFactory.getLogger(NoopTraceVisitor.class);
		logger.setLevel(Level.DEBUG);
		logger.setAdditive(true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testVisits() throws Exception {
		TraceContextHolder.setSampler(new AlwaysSampled());
		TracePoint tp = new TracePoint();
		Thread.sleep(100);
		tp.close();

		Thread.sleep(300);
	}

}
