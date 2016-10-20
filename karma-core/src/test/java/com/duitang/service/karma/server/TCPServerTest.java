package com.duitang.service.karma.server;

import java.net.Socket;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.ReflectRPCHandler;
import com.duitang.service.karma.router.JavaRouter;
import com.duitang.service.karma.trace.NoopTraceVisitor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TCPServerTest {

	@Before
	public void setUp() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		Logger logger = (Logger) LoggerFactory.getLogger(NoopTraceVisitor.class);
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public void test1() throws KarmaException, InterruptedException {
		int port = 7778;
		ServiceConfig conf = new ServiceConfig();
		MemoryCacheService mms = new MemoryCacheService();
		mms.memory_setString("aaaa", "bbbb", 5000);
		System.out.println("aaaa ---> " + mms.memory_getString("aaaa"));

		conf.addService(IDemoService.class, mms);

		ReflectRPCHandler rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();

		JavaRouter rt = new JavaRouter();
		rt.setHandler(rpc);

		TCPServer tcps = new TCPServer();
		tcps.setGroup("dev1");
		tcps.setRouter(rt);
		tcps.setPort(port);
		tcps.start();

		Thread.sleep(200);

		Assert.assertTrue(isPortInUse("localhost", port));

		Assert.assertTrue(tcps.getGroup().equals("dev1"));
		Assert.assertNotNull(tcps.getServiceProtocol());
		Assert.assertNotNull(tcps.getServiceURL());
		Assert.assertTrue(tcps.getPort() == port);

		tcps.stop();
		Assert.assertFalse(isPortInUse("localhost", port));

	}

	boolean isPortInUse(String host, int port) {
		// Assume no connection is possible.
		boolean result = false;

		try {
			(new Socket(host, port)).close();
			result = true;
		} catch (Exception e) {
			// Could not connect.
		}

		return result;
	}

}
