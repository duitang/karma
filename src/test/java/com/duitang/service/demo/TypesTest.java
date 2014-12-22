package com.duitang.service.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.base.ClientFactory;
import com.duitang.service.base.ServerBootstrap;
import com.duitang.service.demo.domain.Demo1;
import com.duitang.service.demo.domain.Demo1Impl;
import com.duitang.service.demo.domain.Demo2;
import com.duitang.service.demo.domain.Demo2Impl;
import com.duitang.service.demo.domain.Demo3;
import com.duitang.service.demo.domain.Demo3Impl;

public class TypesTest {

	@Before
	public void setUp() {
	}

	@After
	public void destroy() {

	}

	/**
	 * primary types
	 * 
	 * @throws Exception
	 */
	@Test
	public void test0() throws Exception {
		ServerBootstrap server = new ServerBootstrap();
		Demo1 service = new Demo1Impl();
		server.addService(Demo1.class, service);
		server.startUp(9998);
		Thread.sleep(100);

		ClientFactory<Demo1> fac = ClientFactory.createFactory(Demo1.class);
		fac.setTimeout(500);
		fac.setUrl("localhost:9999");
		Demo1 cli = fac.create();

		int a1 = 1;
		int[] a2 = new int[] { 2, 3 };
		Assert.assertEquals(service.m_a1(a1, a2), cli.m_a1(a1, a2));
		Assert.assertEquals(Arrays.toString(service.m_a2(a1, a2)), Arrays.toString(cli.m_a2(a1, a2)));

		boolean b1 = true;
		boolean[] b2 = new boolean[] { true, false };
		Assert.assertEquals(service.m_b1(b1, b2), cli.m_b1(b1, b2));
		Assert.assertEquals(Arrays.toString(service.m_b2(b1, b2)), Arrays.toString(cli.m_b2(b1, b2)));

		long c1 = 100;
		long[] c2 = new long[] { 1, 2 };
		Assert.assertEquals(service.m_c1(c1, c2), cli.m_c1(c1, c2));
		Assert.assertEquals(Arrays.toString(service.m_c2(c1, c2)), Arrays.toString(cli.m_c2(c1, c2)));

		float d1 = 211.2f;
		float[] d2 = new float[] { 1.1f, 2.2f };
		Assert.assertEquals(service.m_d1(d1, d2), cli.m_d1(d1, d2));
		Assert.assertEquals(Arrays.toString(service.m_d2(d1, d2)), Arrays.toString(cli.m_d2(d1, d2)));

		double e1 = 3.13;
		double[] e2 = new double[] { 1.1d, 2.2d };
		Assert.assertEquals(service.m_e1(e1, e2), cli.m_e1(e1, e2));
		Assert.assertEquals(Arrays.toString(service.m_e2(e1, e2)), Arrays.toString(cli.m_e2(e1, e2)));

		short f1 = 4;
		short[] f2 = new short[] { 1, 2 };
		Assert.assertEquals(service.m_f1(f1, f2), cli.m_f1(f1, f2));
		Assert.assertEquals(Arrays.toString(service.m_f2(f1, f2)), Arrays.toString(cli.m_f2(f1, f2)));

		char g1 = 'a';
		char[] g2 = { 'b', 'c' };
		Assert.assertEquals(service.m_g1(g1, g2), cli.m_g1(g1, g2));
		Assert.assertEquals(Arrays.toString(service.m_g2(g1, g2)), Arrays.toString(cli.m_g2(g1, g2)));

		byte h1 = 126;
		byte[] h2 = { 3, 4 };
		Assert.assertEquals(service.m_h1(h1, h2), cli.m_h1(h1, h2));
		Assert.assertEquals(Arrays.toString(service.m_h2(h1, h2)), Arrays.toString(cli.m_h2(h1, h2)));

		fac.release(cli);
		server.shutdown();
	}

	/**
	 * list, array, set, map
	 * 
	 * @throws Exception
	 */
	@Test
	public void test1() throws Exception {
		ServerBootstrap server = new ServerBootstrap();
		Demo2 service = new Demo2Impl();

		server.addService(Demo2.class, service);
		server.startUp(9998);
		Thread.sleep(100);

		ClientFactory<Demo2> fac = ClientFactory.createFactory(Demo2.class);
		fac.setTimeout(500);
		fac.setUrl("localhost:9999");
		Demo2 cli = fac.create();

		HashMap<String, Float> p1 = new HashMap<String, Float>();
		p1.put("aa", 1.2F);
		p1.put("bb", 3.4F);
		Assert.assertTrue(comareMap(service.m1(p1), cli.m1(p1)));

		ArrayList<Float> p2 = new ArrayList<Float>();
		p2.add(1.1F);
		p2.add(2.2F);
		p2.add(3.3F);
		Assert.assertEquals(service.m2(p2).toString(), cli.m2(p2).toString());

		Set<Integer> p3 = new HashSet<Integer>();
		p3.add(6);
		p3.add(8);
		p3.add(9);
		Assert.assertEquals((service.m3(p3)).toString(), (cli.m3(p3)).toString());

		String[] p4 = { "1.1", "2.2", "3.3" };
		Assert.assertEquals(Arrays.toString(service.m4(p4)), Arrays.toString(cli.m4(p4)));

		fac.release(cli);
		server.shutdown();
	}

	/**
	 * complex types
	 * 
	 * @throws Exception
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

		ClientFactory<Demo3> fac = ClientFactory.createFactory(Demo3.class);
		fac.setTimeout(500);
		fac.setUrl("localhost:9999");
		Demo3 cli = fac.create();
		DemoObject ret1 = service.getObject(obj);
		DemoObject ret2 = cli.getObject(obj);
		Assert.assertEquals(ret1.b_v, ret2.b_v);
		Assert.assertEquals(Arrays.toString(ret1.bs_v), Arrays.toString(ret2.bs_v));
		Assert.assertEquals(ret1.f_v, ret2.f_v);
		Assert.assertEquals(ret1.i_v, ret2.i_v);
		Assert.assertEquals(ret1.l_v, ret2.l_v);
		Assert.assertEquals(ret1.m_v, ret2.m_v);
		fac.release(cli);

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

}
