package com.duitang.service.karma.handler;

import java.util.ArrayList;
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
			public String hello(String name, double val) {
				if (name == null) {
					throw new RuntimeException("parameter name is null!");
				}
				return "hello, " + name + "; " + val;
			}

			@Override
			public List<String> happy(String name, List<Integer> ids) {
				List<String> ret = new ArrayList<String>();
				for (Integer id : ids) {
					ret.add(name + id);
				}
				return ret;
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
		String param = generateParam(Arrays.asList((Object) "baba", 11.18f));
		ctx.params = new Object[] { param };
		handler.invoke(ctx);

		System.out.println(ctx.ret);
		Assert.assertEquals("hello, " + "baba" + "; " + 11.18d, decodeReturn(ctx.ret));

		ctx = new RPCContext();
		ctx.name = "com.duitang.service.karma.handler.A";
		ctx.method = "hello";
		ctx.invoker = ivk;
		param = generateParam(Arrays.asList((Object) null, 11.18d));
		ctx.params = new Object[] { param };
		try {
			handler.invoke(ctx);
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNull(ctx.ret);

		test1(ivk, handler);
	}

	static void test1(IgnCaseInvoker ivk, JsonRPCHandler handler) throws Exception {
		RPCContext ctx = new RPCContext();
		ctx.name = "com.duitang.service.karma.handler.A";
		ctx.method = "happy";
		ctx.invoker = ivk;
		List<Integer> ids = Arrays.asList(111, 222);
		String param = generateParam(Arrays.asList((Object) "baba", ids));
		ctx.params = new Object[] { param };
		handler.invoke(ctx);
		Assert.assertNotNull(ctx.ret);
		System.out.println(ctx.ret);
		List lst = decodeReturnValue(ctx.ret, List.class);
		System.out.println(lst);
	}

	static String generateParam(List<Object> params) throws Exception {
		return mapper.writeValueAsString(params);
	}

	static String decodeReturn(Object r) throws Exception {
		return mapper.readValue(r.toString(), String.class);
	}

	static <T> T decodeReturnValue(Object r, Class<T> clz) throws Exception {
		return mapper.readValue(r.toString(), clz);
	}

}

interface A {
	String hello(String name, double val);

	List<String> happy(String name, List<Integer> ids);

}
