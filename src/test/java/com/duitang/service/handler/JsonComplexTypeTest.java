package com.duitang.service.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

	final static ObjectMapper mapper = new ObjectMapper();

	protected JsonRPCHandler getHandler(ServiceConfig conf) throws KarmaException {
		ReflectRPCHandler service = new ReflectRPCHandler();
		service.setConf(conf);
		service.init();
		JsonRPCHandler ret = new JsonRPCHandler(service);
		return ret;
	}

	protected String genParam(Object... params) throws Exception {
		return mapper.writeValueAsString(params);
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

		val = genParam(100, Arrays.asList(new Integer[] { 200, 300 }));
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_a1(100, Arrays.asList(new Integer[] { 200, 300 })) == Integer.valueOf(ctx.ret.toString()));

		ctx.method = "m_a2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_a2(100, Arrays.asList(new Integer[] { 200, 300 }))), Arrays.toString(mapper.readValue(ctx.ret.toString(), Integer[].class)));

		ctx.method = "m_b1";
		val = genParam(true, Arrays.asList(new Boolean[] { true, false }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_b1(true, Arrays.asList(new Boolean[] { true, false })) == Boolean.valueOf(ctx.ret.toString()));

		ctx.method = "m_b2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_b2(true, Arrays.asList(new Boolean[] { true, false }))), Arrays.toString(mapper.readValue(ctx.ret.toString(), Boolean[].class)));

		ctx.method = "m_c1";
		val = genParam(300, Arrays.asList(new Long[] { 100L, 200L }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_c1(300L, Arrays.asList(new Long[] { 100L, 200L })) == Long.valueOf(ctx.ret.toString()));

		ctx.method = "m_c2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_c2(300L, Arrays.asList(new Long[] { 100L, 200L }))), Arrays.toString(mapper.readValue(ctx.ret.toString(), Long[].class)));

		ctx.method = "m_d1";
		val = genParam(300.1f, Arrays.asList(new Float[] { 100.2f, 200.3f }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_d1(300.1f, Arrays.asList(new Float[] { 100.2f, 200.3f })) == Float.valueOf(ctx.ret.toString()));

		ctx.method = "m_d2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_d2(300.1f, Arrays.asList(new Float[] { 100.2f, 200.3f }))), Arrays.toString(mapper.readValue(ctx.ret.toString(), Float[].class)));

		ctx.method = "m_e1";
		val = genParam(300.1d, Arrays.asList(new Double[] { 100.2d, 200.3d }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_e1(300.1d, Arrays.asList(new Double[] { 100.2d, 200.3d })) == Double.valueOf(ctx.ret.toString()));

		ctx.method = "m_e2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_e2(300.1d, Arrays.asList(new Double[] { 100.2d, 200.3d }))), Arrays.toString(mapper.readValue(ctx.ret.toString(), Double[].class)));

		ctx.method = "m_f1";
		val = genParam((short) 300, Arrays.asList(new Short[] { 100, 200 }));
		ctx.params = new Object[] { val };
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertTrue(service.m_f1((short) 300, Arrays.asList(new Short[] { 100, 200 })) == Short.valueOf(ctx.ret.toString()));

		ctx.method = "m_f2";
		handler.lookUp(ctx);
		handler.invoke(ctx);
		Assert.assertEquals(Arrays.toString(service.m_f2((short) 300, Arrays.asList(new Short[] { 100, 200 }))), Arrays.toString(mapper.readValue(ctx.ret.toString(), Short[].class)));

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
		ctx.method = "m1";

		Map d = new HashMap();
		d.put("aaa", 1.1f);
		d.put("bbb", 2);
		String val = genParam(d);
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		Map<String, Long> r1 = service.m1(d);
		Map<String, Long> r2 = (Map<String, Long>) mapper.readValue(ctx.ret.toString(), HashMap.class);

		Assert.assertTrue(r1.keySet().size() == r2.keySet().size());
		Assert.assertTrue(r1.values().size() == r2.values().size());
		Assert.assertTrue(diffSetByString(r1.keySet(), r2.keySet()).isEmpty());
		Assert.assertTrue(diffSetByString(r2.keySet(), r1.keySet()).isEmpty());
		Assert.assertTrue(diffSetByString(r1.values(), r2.values()).isEmpty());
		Assert.assertTrue(diffSetByString(r2.values(), r1.values()).isEmpty());

		List<Float> dd = new ArrayList<Float>();
		dd.add(1.1f);
		dd.add(2.2f);
		val = genParam(dd);
		ctx.params = new Object[] { val };
		ctx.method = "m2";

		handler.lookUp(ctx);
		handler.invoke(ctx);

		diffListByString(service.m2(dd), mapper.readValue(ctx.ret.toString(), ArrayList.class));

		List<Integer> ddd = new ArrayList<Integer>();
		ddd.add(1);
		ddd.add(2);
		val = genParam(ddd);
		ctx.params = new Object[] { val };
		ctx.method = "m3";

		handler.lookUp(ctx);
		handler.invoke(ctx);

		Assert.assertTrue(diffSetByString(service.m3(ddd), mapper.readValue(ctx.ret.toString(), HashSet.class)).isEmpty());

		List<Boolean> dddd = new ArrayList<Boolean>();
		dddd.add(true);
		dddd.add(false);
		val = genParam(dddd);
		ctx.params = new Object[] { val };
		ctx.method = "m4";

		handler.lookUp(ctx);
		handler.invoke(ctx);

		Assert.assertEquals(Arrays.toString(service.m4(dddd)), Arrays.toString(mapper.readValue(ctx.ret.toString(), Double[].class)));

	}

	@Test
	public void test2() throws KarmaException, Exception {
		ServiceConfig conf = new ServiceConfig();
		Demo3Impl service = new Demo3Impl();
		conf.addService(Demo3.class, service);

		JsonRPCHandler handler = getHandler(conf);

		RPCContext ctx = new RPCContext();
		ctx.name = Demo3.class.getName();
		ctx.method = "m1";

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
		val = genParam(obj);
		ctx.params = new Object[] { val };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		Assert.assertEquals(service.m1(obj).toString(), mapper.readValue(ctx.ret.toString(), DemoObject.class).toString());

		ctx.method = "m2";
		ctx.params = new Object[] { genParam(Arrays.asList(new DemoObject[] { obj, obj })) };

		handler.lookUp(ctx);
		handler.invoke(ctx);

		List<DemoObject> r1 = service.m2(Arrays.asList(new DemoObject[] { obj, obj }));
		Assert.assertEquals(mapper.writeValueAsString(r1), ctx.ret.toString());

		ctx.method = "m3";
		Map<String, DemoObject> data = new HashMap<String, DemoObject>();
		data.put("aaa", obj);
		data.put("bbb", obj);
		ctx.params = new Object[] { genParam(data) };

		handler.lookUp(ctx);
		handler.invoke(ctx);
		Map<String, DemoObject> r3 = service.m3(data);
		System.out.println(ctx.ret.toString());
		Assert.assertEquals(mapper.writeValueAsString(r3), ctx.ret.toString());

		ctx.method = "m4";
		Map<String, Long> data1 = new HashMap<String, Long>();
		data1.put("aaa", 2L);
		data1.put("bbb", 3L);
		Map<String, Float> data2 = new HashMap<String, Float>();
		data2.put("aaa", 2f);
		data2.put("bbb", 3f);
		ctx.params = new Object[] { genParam(data2) };

		handler.lookUp(ctx);
		handler.invoke(ctx);
		Map<String, Float> r4 = service.m4(data1);
		System.out.println(ctx.ret.toString());
		Assert.assertEquals(mapper.writeValueAsString(r4), ctx.ret.toString());
	}

	static Set diffSetByString(Collection src, Collection test) {
		Set ret = new HashSet();
		for (Object aa : src) {
			ret.add(aa.toString());
		}
		for (Object bb : test) {
			ret.remove(bb.toString());
		}
		return ret;
	}

	static boolean diffListByString(List a, List b) {
		Collections.sort(a);
		Collections.sort(b);
		return a.toString().equals(b.toString());
	}

}

class AB {
	protected String aa;
}

interface B {
	List<String> getStr(ArrayList<Float> data, String a, LinkedList<Long> ddd);
}