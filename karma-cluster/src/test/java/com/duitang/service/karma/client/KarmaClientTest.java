package com.duitang.service.karma.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.karma.KarmaException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class KarmaClientTest {

	@Test
	public void test() throws KarmaException, IOException, Exception {
		Logger root = (Logger) LoggerFactory.getLogger(KarmaClient.class);
		root.setLevel(Level.INFO);
		List<String> urls = Arrays.asList(new String[] { "localhost:9999" });
		KarmaClient<IDemoService> cli = KarmaClient.createKarmaClient(IDemoService.class, urls, "dev1");
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
