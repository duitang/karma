package com.duitang.service.server;

import org.junit.Test;

import com.duitang.service.KarmaException;
import com.duitang.service.demo.DemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.handler.JsonRPCHandler;
import com.duitang.service.handler.ReflectRPCHandler;
import com.duitang.service.router.JsonRouter;

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
		http.setPort(9999);
		http.start();

		Thread.sleep(10000000);
	}

}
