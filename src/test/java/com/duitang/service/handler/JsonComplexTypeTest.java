package com.duitang.service.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.duitang.service.KarmaException;
import com.duitang.service.demo.DemoObject;
import com.duitang.service.demo.domain.Demo1;
import com.duitang.service.demo.domain.Demo1Impl;
import com.duitang.service.demo.domain.Demo2;
import com.duitang.service.demo.domain.Demo2Impl;
import com.duitang.service.demo.domain.Demo3;
import com.duitang.service.demo.domain.Demo3Impl;
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

	// @Test
	public void test0() throws Exception {
		ServiceConfig conf = new ServiceConfig();
		Demo1Impl service = new Demo1Impl();
		conf.addService(Demo1.class, service);

		JsonRPCHandler handler = getHandler(conf);

		RPCContext ctx = new RPCContext();
		String val = null;
		ctx.name = Demo1.class.getName();
		ctx.method = "m_a1";

		val = genParam(Arrays.asList(new Object[] { 100, new int[] { 200, 300 } }));
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_a1(100, new int[] { 200, 300 }) == Integer.valueOf(ctx.ret.toString()));

		ctx.method = "m_a2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_a2(100, new int[] { 200, 300 })), Arrays.toString(mapper.readValue(ctx.ret.toString(), Integer[].class)));

		ctx.method = "m_b1";
		val = genParam(Arrays.asList(new Object[] { true, new boolean[] { true, false } }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_b1(true, new boolean[] { true, false }) == Boolean.valueOf(ctx.ret.toString()));

		ctx.method = "m_b2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_b2(true, new boolean[] { true, false })), Arrays.toString(mapper.readValue(ctx.ret.toString(), Boolean[].class)));

		ctx.method = "m_c1";
		val = genParam(Arrays.asList(new Object[] { 300, new long[] { 100, 200 } }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_c1(300L, new long[] { 100L, 200L }) == Long.valueOf(ctx.ret.toString()));

		ctx.method = "m_c2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_c2(300, new long[] { 100, 200 })), Arrays.toString(mapper.readValue(ctx.ret.toString(), Long[].class)));

		ctx.method = "m_d1";
		val = genParam(Arrays.asList(new Object[] { 300.1f, new float[] { 100.2f, 200.3f } }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_d1(300.1f, new float[] { 100.2f, 200.3f }) == Float.valueOf(ctx.ret.toString()));

		ctx.method = "m_d2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_d2(300.1f, new float[] { 100.2f, 200.3f })), Arrays.toString(mapper.readValue(ctx.ret.toString(), Float[].class)));

		ctx.method = "m_e1";
		val = genParam(Arrays.asList(new Object[] { 300.1d, new double[] { 100.2d, 200.3d } }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_e1(300.1d, new double[] { 100.2d, 200.3d }) == Double.valueOf(ctx.ret.toString()));

		ctx.method = "m_e2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_e2(300.1d, new double[] { 100.2d, 200.3d })), Arrays.toString(mapper.readValue(ctx.ret.toString(), Double[].class)));

		ctx.method = "m_f1";
		val = genParam(Arrays.asList(new Object[] { (short) 300, new short[] { 100, 200 } }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_f1((short) 300, new short[] { 100, 200 }) == Short.valueOf(ctx.ret.toString()));

		ctx.method = "m_f2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_f2((short) 300, new short[] { 100, 200 })), Arrays.toString(mapper.readValue(ctx.ret.toString(), Short[].class)));

		ctx.method = "m_g1";
		ctx.params = new Object[] { "[{\"v\":\"a\"},{\"v\":[\"b\", \"c\"]}]" };
		// handler.lookUp(ctx);
		// handler.invoke(ctx);
		// Assert.assertTrue(String.valueOf(service.m_g1('a', new char[] { 'b',
		// 'c' })) == ctx.ret.toString());

		ctx.method = "m_g2";
		// handler.lookUp(ctx);
		// handler.invoke(ctx);
		// Assert.assertEquals(Arrays.toString(service.m_g2('a', new char[] {
		// 'b', 'c' })), Arrays.toString(mapper.readValue(ctx.ret.toString(),
		// Character[].class)));

		ctx.method = "m_h1";
		// val = genParam(Arrays.asList(new Object[] { (byte) 300, new byte[] {
		// 100, 127 } }));
		// ctx.params = new Object[] { val };
		// handler.lookUp(ctx);
		// handler.invoke(ctx);
		// Assert.assertTrue(service.m_h1((byte) 300, new byte[] { 100, 127 })
		// == Byte.valueOf(ctx.ret.toString()));

		ctx.method = "m_h2";
		// handler.lookUp(ctx);
		// handler.invoke(ctx);
		// Assert.assertEquals(Arrays.toString(service.m_h2((byte) 300, new
		// byte[] { 100, 127 })),
		// Arrays.toString(mapper.readValue(ctx.ret.toString(), Byte[].class)));

	}

	// @Test
	public void test1() throws KarmaException, Exception {
		ServiceConfig conf = new ServiceConfig();
		Demo2Impl service = new Demo2Impl();
		conf.addService(Demo2.class, service);

		JsonRPCHandler handler = getHandler(conf);

		RPCContext ctx = new RPCContext();
		ctx.name = Demo2.class.getName();
		ctx.method = "m2";

		String val = null;
		val = "[{\"v\": [2,3,4]}]";
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		Assert.assertEquals(service.m2(Arrays.asList(new Float[] { 2f, 3f, 4f })).toString(), mapper.readValue(ctx.ret.toString(), ArrayList.class).toString());

		val = "[{\"v\": [2,3,4]}]";
		ctx.params = new Object[] { val };
		ctx.method = "m3";

		handler.lookUp(ctx);
		handler.invoke(ctx);

		HashSet s = new HashSet();
		s.add(2);
		s.add(3);
		s.add(4);

		Set<Float> s1 = service.m3(s);
		HashSet s2 = mapper.readValue(ctx.ret.toString(), HashSet.class);
		Assert.assertTrue(s2.size() == s1.size());
		for (Object n : s2) {
			if (n instanceof Number) {
				float f = ((Number) n).floatValue();
				Assert.assertTrue(s1.contains(f));
			}
		}

		val = "[{\"v\": [\"2\",\"3\",\"4\"]}]";
		ctx.params = new Object[] { val };
		ctx.method = "m4";

		handler.lookUp(ctx);
		handler.invoke(ctx);

		String[] ss = { "2", "3", "4" };

		Double[] r4 = service.m4(ss);
		Assert.assertEquals(Arrays.toString(r4), Arrays.toString(mapper.readValue(ctx.ret.toString(), Double[].class)));

	}

	@Test
	public void test2() throws KarmaException, Exception {
		ServiceConfig conf = new ServiceConfig();
		Demo3Impl service = new Demo3Impl();
		conf.addService(Demo3.class, service);

		JsonRPCHandler handler = getHandler(conf);

		RPCContext ctx = new RPCContext();
		ctx.name = Demo3.class.getName();
		ctx.method = "getObject";

		String val = null;
		DemoObject obj = new DemoObject();
		obj.setB_v(true);
		obj.setDomain("somedomain");
		obj.setF_v(1.1f);
		obj.setI_v(12);
		obj.setL_v(1113);
		obj.setMethod("aaa");
		obj.setM_v(new HashMap());
		obj.getM_v().put("111", "bbb");
		val = genParam(Arrays.asList(new Object[] { mapper.writeValueAsString(obj) }));
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		Assert.assertEquals(service.getObject(obj).toString(), mapper.readValue(ctx.ret.toString(), DemoObject.class).toString());

		ctx.method = "getObjects";
		val = genParam(Arrays.asList(new Object[] { mapper.writeValueAsString(obj), mapper.writeValueAsString(obj) }));
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		Assert.assertEquals(service.getObjects(Arrays.asList(new DemoObject[] { obj, obj })).toString(), mapper.readValue(ctx.ret.toString(), ArrayList.class).toString());

	}
}

class AB {
	protected String aa;
}

interface B {
	List<String> getStr(ArrayList<Float> data, String a, LinkedList<Long> ddd);
}