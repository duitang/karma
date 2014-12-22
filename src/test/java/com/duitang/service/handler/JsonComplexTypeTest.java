package com.duitang.service.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.duitang.service.KarmaException;
import com.duitang.service.demo.domain.Demo1;
import com.duitang.service.demo.domain.Demo1Impl;
import com.duitang.service.demo.domain.Demo2;
import com.duitang.service.demo.domain.Demo2Impl;
import com.duitang.service.server.ServiceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonComplexTypeTest {

	static ObjectMapper mapper = new ObjectMapper();

	protected JsonRPCHandler getHandler(ServiceConfig conf) throws KarmaException {
		ReflectRPCHandler service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();
		JsonRPCHandler ret = new JsonRPCHandler(service);
		return ret;
	}

	protected String genParam(List param) throws Exception {
		List<Map<String, Object>> p = new ArrayList<Map<String, Object>>();
		for (Object par : param) {
			Map m = new HashMap<String, Object>();
			m.put("v", par);
			p.add(m);
		}
		return mapper.writeValueAsString(p);
	}

	@Test
	public void test0() throws Exception {
		ServiceConfig conf = new ServiceConfig();
		Demo1Impl service = new Demo1Impl();
		conf.addService(Demo1.class, service);

		JsonRPCHandler handler = getHandler(conf);

		RPCContext ctx = new RPCContext();
		ctx.name = Demo1.class.getName();
		ctx.method = "m_a1";

		String val = genParam(Arrays.asList(new Object[] { 100, new int[] { 200, 300 } }));
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		System.out.println(ctx.ret);

	}

	@Test
	public void test1() throws KarmaException {
		ServiceConfig conf = new ServiceConfig();
		Demo2Impl service = new Demo2Impl();
		conf.addService(Demo2.class, service);

		JsonRPCHandler handler = getHandler(conf);

		RPCContext ctx = new RPCContext();
		ctx.name = Demo2.class.getName();
		ctx.method = "m2";

		String val = "[{\"v\": [2,3,4]}]";
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		System.out.println(ctx.ret);
	}

}

class AB {
	protected String aa;
}

interface B {
	List<String> getStr(ArrayList<Float> data, String a, LinkedList<Long> ddd);
}