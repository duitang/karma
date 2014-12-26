package com.duitang.service.client;

import java.io.IOException;
import java.util.HashSet;

import org.junit.Test;

import com.duitang.service.KarmaException;
import com.duitang.service.demo.DemoService;

public class KarmaClientTest {

	@Test
	public void test() throws KarmaException, IOException {
		KarmaIoSession session = new KarmaIoSession("localhost:9999", 500);
		session.init();
		KarmaClient<DemoService> cli = KarmaClient.createKarmaClient(DemoService.class, session);
		DemoService client = cli.getService();
		System.out.println(client.memory_getString("aaaa"));
		System.out.println(client.trace_msg("laurence", 200));
		System.out.println(client.noparam());
		System.out.println(client.getM(new HashSet()));
		try {
			System.out.println(client.trace_msg("laurence", 600));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println(client.memory_setBytes("aaa", "fuck".getBytes(), 5000));
		System.out.println(new String(client.memory_getBytes("aaa")));
		cli.close();
	}

}
