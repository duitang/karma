package com.duitang.service.karma.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.duitang.service.demo.DemoServer;
import com.duitang.service.karma.trace.NoopTraceVisitor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class KarmaIoSessionTest {

	@Before
	public void setUp() throws Exception {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		Logger logger = (Logger) LoggerFactory.getLogger(NoopTraceVisitor.class);
		logger.setLevel(Level.DEBUG);
		logger = (Logger) LoggerFactory.getLogger(KarmaClient.class);
		logger.setLevel(Level.DEBUG);
		DemoServer.startUp();
	}

	@After
	public void destroy() {
		DemoServer.shutdown(); // testing shutdown
	}

	@Test
	public void test() throws Exception {
		KarmaIoSession session;
		session = new KarmaIoSession("localhost:9999", 500);
		session.init();
		boolean ret = false;
		ret = session.isConnected();
		System.out.println(ret);
		Assert.assertTrue(ret);
		ret = session.isAlive();
		System.out.println(ret);
		Assert.assertTrue(ret);
		session.close();
		ret = session.isConnected();
		System.out.println(ret);
		Assert.assertFalse(ret);
		ret = session.isAlive();
		System.out.println(ret);
		Assert.assertFalse(ret);

		KarmaIoSession.shutdown();
	}

	@Test
	public void test2() throws Exception {
		KarmaIoSession session = new KarmaIoSession("localhost:9999", 500);
		session.init();
		Assert.assertTrue(session.ping());
		Assert.assertTrue(session.reachable());

		System.out.println(session);

		DemoServer.shutdown(); // force shutdown

		Assert.assertFalse(session.ping());
		Assert.assertTrue(session.reachable());

		System.out.println(session);
		session.close();
	}

}
