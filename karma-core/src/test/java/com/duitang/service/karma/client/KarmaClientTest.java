package com.duitang.service.karma.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.KarmaClientConfig;
import com.duitang.service.karma.trace.NoopTraceVisitor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class KarmaClientTest {

	@Before
	public void setUp() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		Logger logger = (Logger) LoggerFactory.getLogger(NoopTraceVisitor.class);
		logger.setLevel(Level.DEBUG);
		logger = (Logger) LoggerFactory.getLogger(KarmaClient.class);
		logger.setLevel(Level.DEBUG);
	}

	@Test
	public void test() throws KarmaException, IOException, Exception {
		List<String> urls = Arrays.asList(new String[] { "localhost:9999" });
		String group = "dev1";
		KarmaClientConfig.updateBalance(group, urls);
		KarmaClient<IDemoService> cli = KarmaClient.createKarmaClient(IDemoService.class, urls, group);
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

}
