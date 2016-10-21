package com.duitang.service.karma.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.server.ServiceConfig;

public class ReflectRPCHandlerTest {

	ServiceConfig conf;

	@Before
	public void setUp() {
		conf = new ServiceConfig();
		conf.addService(IDemoService.class, new MemoryCacheService());
	}

	@Test
	public void test() throws Exception {
		ReflectRPCHandler service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();

		String domain = IDemoService.class.getName();
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

	@Test
	public void test2() throws Exception {
		ReflectRPCHandler service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();

		System.out.println(service.getConf().getServices());

		String domain = IDemoService.class.getName();
		String method = "memory_setString";
		Object[] param = new Object[] { "aaa", "bbb", 10000 };
		RPCContext ctx = new RPCContext();
		ctx.name = domain + "adf";
		ctx.method = method + "adf";
		ctx.params = param;
		try {
			service.lookUp(ctx);
			Assert.fail();
		} catch (KarmaException e) {
			// ignore
		}
		try {
			service.invoke(ctx);
			Assert.fail();
		} catch (KarmaException e) {
			// ignore
		}
	}

}
