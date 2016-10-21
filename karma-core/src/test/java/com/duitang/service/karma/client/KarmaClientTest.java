package com.duitang.service.karma.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.duitang.service.demo.DemoException;
import com.duitang.service.demo.DemoServer;
import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaRuntimeException;
import com.duitang.service.karma.boot.KarmaClientConfig;
import com.duitang.service.karma.trace.NoopTraceVisitor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class KarmaClientTest {

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
	public void test() throws KarmaException, IOException, Exception {
		List<String> urls = Arrays.asList(new String[] { "localhost:9999" });
		String group = "dev2";
		KarmaClientConfig.bindBalance(group, urls);
		KarmaClient<IDemoService> cli = KarmaClient.createKarmaClient(IDemoService.class, group);
		IDemoService client = cli.getService();
		System.out.println(client.memory_getString("aaaa"));
		System.out.println(client.trace_msg("laurence", 200));
		System.out.println(client.noparam());
		System.out.println(client.getM(new HashSet()));
		try {
			System.out.println(client.trace_msg("laurence", 600));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			client.getError();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println(client.memory_setBytes("aaa", "fuck".getBytes(), 5000));
		System.out.println(new String(client.memory_getBytes("aaa")));

		// Thread.sleep(100000);
	}

	@Test
	public void test1() throws Exception {
		try {
			KarmaClient.createKarmaClient(MemoryCacheService.class, Arrays.asList("localhost:9999"));
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e instanceof KarmaException);
		}

		KarmaClient<IDemoService> cli = KarmaClient.createKarmaClient(IDemoService.class,
				Arrays.asList("localhost:9999"));
		long to = 2000;
		cli.setTimeout(to);
		Assert.assertTrue(cli.getTimeout() == to + 1);
		try {
			cli.getService().getExp();
			Assert.fail();
		} catch (DemoException e) {
			e.printStackTrace();
		}

		try {
			cli.getService().getTimeoutError(to);
			Assert.fail();
		} catch (KarmaRuntimeException e) {
			e.printStackTrace();
		}

		KarmaClient.bindGroup("dev2", Arrays.asList("localhost:9999"));
		cli.resetTrace();

		try {
			cli.getService().getExp();
			Assert.fail();
		} catch (DemoException e) {
			e.printStackTrace();
		}

		try {
			cli.getService().getTimeoutError(to);
			Assert.fail();
		} catch (KarmaRuntimeException e) {
			e.printStackTrace();
		}

	}

}
