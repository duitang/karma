package com.duitang.service.karma.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.base.ServerBootstrap;
import com.duitang.service.karma.demo.DemoJsonRPCImpl;
import com.duitang.service.karma.demo.DemoJsonRPCService;
import com.duitang.service.karma.demo.DemoRPCDTO;
import com.duitang.service.karma.demo.DemoService;
import com.duitang.service.karma.demo.MemoryCacheService;
import com.duitang.service.karma.demo.domain.SimpleObject;
import com.duitang.service.karma.handler.JsonRPCHandler;
import com.duitang.service.karma.handler.ReflectRPCHandler;
import com.duitang.service.karma.router.JsonRouter;
import com.duitang.service.karma.server.HTTPServer;
import com.duitang.service.karma.server.ServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HTTPServerTest {

	@Test
	public void test() throws KarmaException, InterruptedException {
		ServiceConfig conf = new ServiceConfig();
		MemoryCacheService mms = new MemoryCacheService();
		mms.memory_setString("aaaa", "bbbb", 5000);
		System.out.println("aaaa ---> " + mms.memory_getString("aaaa"));

		conf.addService(DemoService.class, mms);

		ReflectRPCHandler rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();

		JsonRPCHandler jhl = new JsonRPCHandler(rpc);
		JsonRouter rt = new JsonRouter();
		rt.setHandler(jhl);

		HTTPServer http = new HTTPServer();
		http.setRouter(rt);
		http.setPort(9998);
		http.start();

		Thread.sleep(10000000);
	}

	// @Test
	public void test1() throws KarmaException, InterruptedException {
		ServiceConfig conf = new ServiceConfig();
		TestPlay mms = new TestPlay() {

			@Override
			public boolean play(List<Float> n, SimpleObject obj, String msg) {
				System.out.println(n);
				System.out.println(obj);
				System.out.println(msg);
				return true;
			}

		};

		conf.addService(TestPlay.class, mms);

		ReflectRPCHandler rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();

		JsonRPCHandler jhl = new JsonRPCHandler(rpc);
		JsonRouter rt = new JsonRouter();
		rt.setHandler(jhl);

		HTTPServer http = new HTTPServer();
		http.setRouter(rt);
		http.setPort(9999);
		http.start();

		Thread.sleep(10000000);
	}

//	@Test
	public void test3() throws KarmaException, InterruptedException, Exception {
		ServiceConfig conf = new ServiceConfig();
		DemoJsonRPCService service = new DemoJsonRPCImpl();
		conf.addService(DemoJsonRPCService.class, service);
		StringBuilder sb = new StringBuilder();
		ServerBootstrap.serviceInfo(DemoJsonRPCService.class, sb, "", 0);
		System.out.println(sb.toString());

		ReflectRPCHandler rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();

		JsonRPCHandler jhl = new JsonRPCHandler(rpc);
		JsonRouter rt = new JsonRouter();
		rt.setHandler(jhl);

		HTTPServer http = new HTTPServer();
		http.setRouter(rt);
		http.setPort(9999);
		http.start();

		ObjectMapper mapper = new ObjectMapper();
		DemoRPCDTO a = new DemoRPCDTO();
		a.setA("hello");
		a.setB(new ArrayList(Arrays.asList(new Float[] { 1.1f, 2.2f })));
		a.setC(new HashMap());
		a.getC().put("dd", 3.3d);
		a.getC().put("ee", 4.4d);
		ArrayList<Float> b = new ArrayList<Float>();
		b.add(6.6f);
		b.add(7.7f);
		Object[] params = null;
		params = new Object[] { a, new DemoRPCDTO[] { a, a }, 55L, "laurence", b };
		String src = null;
		src = mapper.writeValueAsString(params);
		System.out.println(src);

		Thread.sleep(10000000);
	}

}

interface TestPlay {

	boolean play(List<Float> n, SimpleObject obj, String msg);

}
