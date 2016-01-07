package com.duitang.service.karma.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.demo.DemoObject;
import com.duitang.service.karma.demo.IDemoService;
import com.duitang.service.karma.demo.MemoryCacheService;
import com.duitang.service.karma.demo.domain.SimpleObject;
import com.duitang.service.karma.server.ServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRPCHandlerTest {

	ServiceConfig conf;
	DemoObject data;
	ReflectRPCHandler service;
	ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() throws Exception {
		conf = new ServiceConfig();
		conf.addService(IDemoService.class, new MemoryCacheService());

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

		String domain = IDemoService.class.getName().toLowerCase();
		String method = "memory_setString".toLowerCase();
		Object[] param = new Object[] { "[{\"v\":\"aaa\"}, {\"v\":\"bbb\"}, {\"v\":\"10000\"}]" };
		RPCContext ctx = new RPCContext();
		ctx.name = domain;
		ctx.method = method;
		ctx.params = param;
		jik.lookUp(ctx);
		jik.invoke(ctx);
		System.out.println(ctx.ret);

		method = "memory_getString";
		param = new Object[] { "[{\"v\":\"aaa\"}]" };
		ctx = new RPCContext();
		ctx.name = domain;
		ctx.method = method;
		ctx.params = param;
		jik.lookUp(ctx);
		jik.invoke(ctx);
		System.out.println(ctx.ret);
	}

	// @Test
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
		RPCContext ctx = new RPCContext();
		ctx.name = domain;
		ctx.method = method;
		ctx.params = param;
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

	@Test
	public void test4() throws KarmaException, Exception {

		// mapper.configure(DeserializationFeature.., true);
		MyTypeService mytp = new MyImpl();
		ServiceConfig conf = new ServiceConfig();
		conf.addService(MyTypeService.class, mytp);

		ReflectRPCHandler service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();
		JsonRPCHandler jik = new JsonRPCHandler(service);

		SimpleObject a = new SimpleObject();
		a.setA("hello");
		a.setB(new ArrayList(Arrays.asList(new Float[] { 1.1f, 2.2f })));
		a.setC(new HashMap());
		a.getC().put("dd", 3.3d);
		a.getC().put("ee", 4.4d);
		ArrayList<Float> b = new ArrayList<Float>();
		b.add(6.6f);
		b.add(7.7f);

		Object[] params = null;
		params = new Object[] { a, new SimpleObject[] { a, a }, 55L, "laurence" };
		String src = null;
		src = mapper.writeValueAsString(params);
		System.out.println(src);

		// src =
		// "[{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": 12}, \"b\": [1.2, 2.2]}, [{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": 3.3}, \"b\": [1, 2]}, {\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": 1.2}, \"b\": [1, 2]}], 111, \"laurence\"]";
		// String src =
		// "[{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": 3.3}, \"b\": [1, 2]}, {\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": \"bbb\"}, \"b\": [1, 2]}]";
		// SimpleObject[] lst = mapper.readValue(src, A.class);
		// System.out.println(lst);
		RPCContext ctx = new RPCContext();
		ctx.name = MyTypeService.class.getName();
		ctx.method = "get1";
		ctx.params = new Object[] { src };
		jik.lookUp(ctx);
		jik.invoke(ctx);
		// System.out.println(ctx.ret);

		params = new Object[] { a, new SimpleObject[] { a, a }, 55L, "laurence", b };
		src = mapper.writeValueAsString(params);
		System.out.println(src);
		ctx = new RPCContext();
		ctx.name = MyTypeService.class.getName();
		ctx.method = "get0";
		ctx.params = new Object[] { src };
		jik.lookUp(ctx);
		jik.invoke(ctx);
	}

	// @Test
	public void test5() throws Exception {
		String src = "{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": 3.3}, \"b\": [1, 2]}";
		HashMap m = mapper.readValue(src, HashMap.class);
		SimpleObject obj = mapper.convertValue(m, SimpleObject.class);
		System.out.println(obj);
		System.out.println(mapper.writeValueAsString(obj));
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

interface MyTypeService {

	SimpleObject get0(SimpleObject obj, List<SimpleObject> lst, Long id, String name, List<Float> score);

	SimpleObject get1(SimpleObject obj, List<SimpleObject> lst, Long id, String name);

}

class MyImpl implements MyTypeService {

	@Override
	public SimpleObject get0(SimpleObject obj, List<SimpleObject> lst, Long id, String name, List<Float> score) {
		System.out.println(obj);
		System.out.println(lst);
		System.out.println(id);
		System.out.println(name);
		System.out.println(score);
		return obj;
	}

	@Override
	public SimpleObject get1(SimpleObject obj, List<SimpleObject> lst, Long id, String name) {
		System.out.println(obj);
		System.out.println(lst);
		System.out.println(id);
		System.out.println(name);
		return obj;
	}

}