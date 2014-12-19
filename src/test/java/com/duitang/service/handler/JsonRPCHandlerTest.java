package com.duitang.service.handler;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.duitang.service.KarmaException;
import com.duitang.service.demo.DemoObject;
import com.duitang.service.demo.DemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.server.ServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRPCHandlerTest {

	ServiceConfig conf;
	DemoObject data;
	ReflectRPCHandler service;

	@Before
	public void setUp() throws Exception {
		conf = new ServiceConfig();
		conf.addService(DemoService.class, new MemoryCacheService());

		service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();

		DemoObject ret = new DemoObject();
		ret.setB_v(true);
		ret.setBs_v("abcd1234".getBytes());
		ret.setF_v(1.2f);
		ret.setI_v(23);
		ret.setL_v(123);
		ret.setM_v(new HashMap());
		ret.getM_v().put("11", "22");
		ret.setDomain("aaa");
		ret.setMethod("bbb");
		data = ret;
	}

	// @Test
	public void test() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String type = DemoObject.class.getName();
		DemoObject d = data;
		String v = mapper.writeValueAsString(d);
		Map m = new HashMap();
		m.put("t", type);
		m.put("v", v);
		String obj = mapper.writeValueAsString(m);
		System.out.println(obj);

		JsonRPCHandler jik = new JsonRPCHandler(service);

		String domain = DemoService.class.getName().toLowerCase();
		String method = "memory_setString".toLowerCase();
		Object[] param = new Object[] { "[{\"v\":\"aaa\"}, {\"v\":\"bbb\"}, {\"v\":\"10000\"}]" };
		RPCContext ctx = new RPCContext(domain, method, param);
		jik.lookUp(ctx);
		jik.invoke(ctx);
		System.out.println(ctx.ret);

		method = "memory_getString";
		param = new Object[] { "[{\"v\":\"aaa\"}]" };
		ctx = new RPCContext(domain, method, param);
		jik.lookUp(ctx);
		jik.invoke(ctx);
		System.out.println(ctx.ret);
	}

	@Test
	public void test2() throws KarmaException {
		ABCD ddd = new MyService();
		ServiceConfig conf = new ServiceConfig();
		conf.addService(ABCD.class, ddd);

		ReflectRPCHandler service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();
		JsonRPCHandler jik = new JsonRPCHandler(service);

		String domain = "com.duitang.service.handler.ABCD".toLowerCase();
		String method = "plaY";
		Object[] param = new Object[] { "[{\"v\":\"fuck\"}, {\"v\":1.98, \"t\": \"java.lang.Long\"}]" };
		RPCContext ctx = new RPCContext(domain, method, param);
		jik.lookUp(ctx);
		jik.invoke(ctx);
		System.out.println(ctx.ret);
	}

	// @Test
	public void test3() {
		// String key = "aaa";
		// String val = "bbb";
		// Long ttl = 100000L;
		//
		// List params = new ArrayList();
		// params.add(getParameterValue(key));
		// params.add(getParameterValue(val));
		// params.add(getParameterValue(ttl));
		// String pp = mapper.writeValueAsString(params);
		//
		// System.out.println("parameter ---------> " + pp);
		//
		// Object rr = jik.invoke("memory_setString", new Object[] { pp });
		// System.out.println(rr);

		// mapper.readValues(jp, valueType)
	}

	protected Map getParameterValue(Object val) {
		Map ret = new HashMap();
		// ret.put("t", val.getClass().getName());
		ret.put("v", val);
		return ret;
	}

}

interface ABCD {
	DemoObject play(String a, float b);
}

class MyService implements ABCD {

	@Override
	public DemoObject play(String a, float b) {
		DemoObject ret = new DemoObject();
		ret.setB_v(true);
		ret.setBs_v("abcd1234".getBytes());
		ret.setF_v(b);
		ret.setI_v(23);
		ret.setL_v(11L);
		ret.setM_v(new HashMap());
		ret.getM_v().put("11", "22");
		ret.setDomain(a);
		ret.setMethod(a);
		return ret;
	}

}