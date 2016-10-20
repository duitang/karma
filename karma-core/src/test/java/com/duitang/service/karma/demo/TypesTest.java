package com.duitang.service.karma.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.duitang.service.demo.DemoObject;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.client.KarmaClient;
import com.duitang.service.karma.demo.domain.Demo1;
import com.duitang.service.karma.demo.domain.Demo1Impl;
import com.duitang.service.karma.demo.domain.Demo2;
import com.duitang.service.karma.demo.domain.Demo2Impl;
import com.duitang.service.karma.demo.domain.Demo3;
import com.duitang.service.karma.demo.domain.Demo3Impl;

public class TypesTest {

	static <T> List<T> asList(Class<T> clz, Object... objs) {
		List<T> ret = new ArrayList<T>();
		for (Object oo : objs) {
			ret.add((T) oo);
		}
		return ret;
	}

	/**
	 * primary types
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void test0() throws Exception {
		ServerBootstrap server = new ServerBootstrap();
		Demo1 service = new Demo1Impl();
		server.addService(Demo1.class, service);
		server.startUp(9998);
		Thread.sleep(100);

		KarmaClient<Demo1> client = KarmaClient.createKarmaClient(Demo1.class, Arrays.asList("localhost:9998"), "dev1");
		Demo1 cli = client.getService();

		int a1 = 1;
		List<Integer> a2 = asList(Integer.class, 2, 3);
		Assert.assertEquals(service.m_a1(a1, a2), cli.m_a1(a1, a2));
		Assert.assertEquals(Arrays.toString(service.m_a2(a1, a2)), Arrays.toString(cli.m_a2(a1, a2)));

		boolean b1 = true;
		List<Boolean> b2 = asList(Boolean.class, true, false);
		Assert.assertEquals(service.m_b1(b1, b2), cli.m_b1(b1, b2));
		Assert.assertEquals(Arrays.toString(service.m_b2(b1, b2)), Arrays.toString(cli.m_b2(b1, b2)));

		long c1 = 100;
		List<Long> c2 = asList(Long.class, 1L, 2L);
		Assert.assertEquals(service.m_c1(c1, c2), cli.m_c1(c1, c2));
		Assert.assertEquals(Arrays.toString(service.m_c2(c1, c2)), Arrays.toString(cli.m_c2(c1, c2)));

		float d1 = 211.2f;
		List<Float> d2 = asList(Float.class, 1.1f, 2.2f);
		Assert.assertTrue(service.m_d1(d1, d2) == cli.m_d1(d1, d2));
		Assert.assertEquals(Arrays.toString(service.m_d2(d1, d2)), Arrays.toString(cli.m_d2(d1, d2)));

		double e1 = 3.13;
		List<Double> e2 = asList(Double.class, 1.1d, 2.2d);
		Assert.assertTrue(service.m_e1(e1, e2) == cli.m_e1(e1, e2));
		Assert.assertEquals(Arrays.toString(service.m_e2(e1, e2)), Arrays.toString(cli.m_e2(e1, e2)));

		short f1 = 4;
		List<Short> f2 = asList(Short.class, (short) 1, (short) 2);
		Assert.assertEquals(service.m_f1(f1, f2), cli.m_f1(f1, f2));
		Assert.assertEquals(Arrays.toString(service.m_f2(f1, f2)), Arrays.toString(cli.m_f2(f1, f2)));

		char g1 = 'a';
		List<Character> g2 = asList(Character.class, 'b', 'c');
		Assert.assertTrue(service.m_g1(g1, g2) == cli.m_g1(g1, g2));
		Assert.assertTrue(StringUtils.equals(Arrays.toString(service.m_g2(g1, g2)), Arrays.toString(cli.m_g2(g1, g2))));

		byte h1 = 126;
		List<Byte> h2 = asList(Byte.class, (byte) 3, (byte) 4);
		Assert.assertTrue(service.m_h1(h1, h2) == cli.m_h1(h1, h2));
		Assert.assertEquals(Arrays.toString(service.m_h2(h1, h2)), Arrays.toString(cli.m_h2(h1, h2)));

		server.shutdown();
	}

	/**
	 * list, array, set, map
	 */
	@Test
	public void test1() throws Exception {
		ServerBootstrap server = new ServerBootstrap();
		Demo2 service = new Demo2Impl();

		server.addService(Demo2.class, service);
		server.startUp(9998);
		Thread.sleep(100);

		KarmaClient<Demo2> client = KarmaClient.createKarmaClient(Demo2.class, Arrays.asList("localhost:9998"), "dev1");
		Demo2 cli = client.getService();

		HashMap<String, Number> p1 = new HashMap<String, Number>();
		p1.put("aa", 1.2F);
		p1.put("bb", 3.4F);
		Assert.assertTrue(comareMap(service.m1(p1), cli.m1(p1)));

		ArrayList<Float> p2 = new ArrayList<Float>();
		p2.add(1.1F);
		p2.add(2.2F);
		p2.add(3.3F);
		Assert.assertEquals(service.m2(p2).toString(), cli.m2(p2).toString());

		ArrayList<Integer> p3 = new ArrayList<Integer>();
		p3.add(6);
		p3.add(8);
		p3.add(9);
		Assert.assertEquals((service.m3(p3)).toString(), (cli.m3(p3)).toString());

		List<Boolean> p4 = asList(Boolean.class, false, true, false);
		Assert.assertEquals(Arrays.toString(service.m4(p4)), Arrays.toString(cli.m4(p4)));

		server.shutdown();
	}

	/**
	 * complex types
	 */
	@Test
	public void test2() throws Exception {
		ServerBootstrap server = new ServerBootstrap();
		Demo3 service = new Demo3Impl();
		server.addService(Demo3.class, service);
		server.startUp(9998);
		Thread.sleep(100);

		DemoObject obj = new DemoObject();
		obj.b_v = true;
		obj.bs_v = null;
		obj.f_v = obj.f_v + 1.1f;
		obj.i_v = obj.i_v + 2;
		obj.l_v = obj.l_v + 11;
		obj.m_v = new HashMap<String, String>();
		obj.m_v.put("linux", "ddd");

		KarmaClient<Demo3> client = KarmaClient.createKarmaClient(Demo3.class, Arrays.asList("localhost:9998"), "dev1");
		Demo3 cli = client.getService();
		DemoObject ret1 = service.m1(obj);
		DemoObject ret2 = cli.m1(obj);
		Assert.assertEquals(ret1.b_v, ret2.b_v);
		Assert.assertEquals(Arrays.toString(ret1.bs_v), Arrays.toString(ret2.bs_v));
		Assert.assertTrue(ret1.f_v == ret2.f_v);
		Assert.assertEquals(ret1.i_v, ret2.i_v);
		Assert.assertEquals(ret1.l_v, ret2.l_v);
		Assert.assertEquals(ret1.m_v, ret2.m_v);

		List<DemoObject> lst = asList(DemoObject.class, obj, obj);
		List<DemoObject> l1 = service.m2(lst);
		List<DemoObject> l2 = cli.m2(lst);
		Assert.assertEquals(l1.size(), l2.size());
		for (int i = 0; i < l1.size(); i++) {
			ret1 = l1.get(i);
			ret2 = l2.get(i);
			Assert.assertEquals(ret1.b_v, ret2.b_v);
			Assert.assertEquals(Arrays.toString(ret1.bs_v), Arrays.toString(ret2.bs_v));
			Assert.assertTrue(ret1.f_v == ret2.f_v);
			Assert.assertTrue(ret1.i_v == ret2.i_v);
			Assert.assertTrue(ret1.l_v == ret2.l_v);
			Assert.assertEquals(ret1.m_v, ret2.m_v);
		}

		Map<String, DemoObject> mm = new HashMap<String, DemoObject>();
		mm.put("aaa", obj);
		mm.put("bbb", obj);
		Map<String, DemoObject> mm1 = service.m3(mm);
		Map<String, DemoObject> mm2 = cli.m3(mm);
		System.out.println(mm1);
		System.out.println(mm2);
		Assert.assertTrue(mm1.keySet().size() == mm2.keySet().size());
		Assert.assertTrue(mm2.values().size() == mm2.values().size());
		Assert.assertTrue(diffSetByString(mm1.keySet(), mm2.keySet()).isEmpty());
		Assert.assertTrue(diffSetByString(mm2.keySet(), mm1.keySet()).isEmpty());
		Assert.assertTrue(diffSetByString(mm1.values(), mm2.values()).isEmpty());
		Assert.assertTrue(diffSetByString(mm2.values(), mm1.values()).isEmpty());
		// Assert.assertTrue(comareMap(mm1, mm2));

		Map<String, Long> p4 = new HashMap<String, Long>();
		p4.put("aaa", 1L);
		p4.put("bbb", 2L);
		Map<String, Float> r41 = service.m4(p4);
		Map<String, Float> r42 = cli.m4(p4);
		Assert.assertTrue(comareMap(r41, r42));

		server.shutdown();
	}

	boolean comareMap(Map a, Map b) {
		if (a.size() != b.size()) {
			return false;
		}
		ArrayList alst = new ArrayList(a.keySet());
		Collections.sort(alst);
		ArrayList blst = new ArrayList(b.keySet());
		Collections.sort(blst);
		if (!alst.equals(blst)) {
			return false;
		}
		alst = new ArrayList(a.values());
		Collections.sort(alst);
		blst = new ArrayList(b.values());
		Collections.sort(blst);
		if (!alst.equals(blst)) {
			return false;
		}
		return true;
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

}
