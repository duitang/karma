package com.duitang.service.handler;

import org.junit.Before;
import org.junit.Test;

import com.duitang.service.demo.DemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.server.ServiceConfig;

public class ReflectRPCHandlerTest {

	ServiceConfig conf;

	@Before
	public void setUp() {
		conf = new ServiceConfig();
		conf.addService(DemoService.class, new MemoryCacheService());
	}

	@Test
	public void test() throws Exception {
		ReflectRPCHandler service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();

		String domain = DemoService.class.getName();
		String method = "memory_setString";
		Object[] param = new Object[] { "aaa", "bbb", 10000 };
		RPCContext ctx = new RPCContext();
		ctx.name = domain;
		ctx.method = method;
		ctx.params = param;

		service.lookUp(ctx);
		service.invoke(ctx);
		System.out.println(ctx.ret);

		method = "memory_getString";
		param = new Object[] { "aaa" };
		ctx.name = domain;
		ctx.method = method;
		ctx.params = param;

		service.lookUp(ctx);
		service.invoke(ctx);
		System.out.println(ctx.ret);
	}
}
