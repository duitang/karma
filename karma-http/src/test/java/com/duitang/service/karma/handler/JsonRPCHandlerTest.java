package com.duitang.service.karma.handler;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.invoker.IgnCaseInvoker;
import com.duitang.service.karma.invoker.ReflectInvoker;
import com.duitang.service.karma.server.ServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRPCHandlerTest {

	final static ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test0() throws Exception {
		ServiceConfig cfg = new ServiceConfig();
		A rpc = new A() {

			@Override
			public String hello(String name) {
				if (name == null) {
					throw new RuntimeException("parameter name is null!");
				}
				return "hello, " + name;
			}

		};
		cfg.addService(A.class, rpc);
		ReflectRPCHandler src = new ReflectRPCHandler();
		src.setConf(cfg);
		src.init();
		JsonRPCHandler handler = new JsonRPCHandler(src);
		ReflectInvoker invoker0 = new ReflectInvoker(A.class, rpc);

		IgnCaseInvoker ivk = new IgnCaseInvoker(invoker0);

		RPCContext ctx = new RPCContext();
		ctx.name = "com.duitang.service.karma.handler.A";
		ctx.method = "hello";
		ctx.invoker = ivk;
		String param = generateParam(Arrays.asList((Object) "baba"));
		ctx.params = new Object[] { param };
		handler.invoke(ctx);

		System.out.println(ctx.ret);
		Assert.assertEquals("hello, " + "baba", decodeReturn(ctx.ret));

		ctx = new RPCContext();
		ctx.name = "com.duitang.service.karma.handler.A";
		ctx.method = "hello";
		ctx.invoker = ivk;
		param = generateParam(Arrays.asList((Object) null));
		ctx.params = new Object[] { param };
		try{			
			handler.invoke(ctx);
			Assert.fail();
		}catch(Exception e){
			e.printStackTrace();
		}
		Assert.assertNull(ctx.ret);
	}

	static String generateParam(List<Object> params) throws Exception {
		return mapper.writeValueAsString(params);
	}

	static String decodeReturn(Object r) throws Exception {
		return mapper.readValue(r.toString(), String.class);
	}

}

interface A {
	String hello(String name);
}
