package com.duitang.service.base;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.Mixin;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMixinServices {

	@Before
	public void setUp() {

	}

	@After
	public void close() {

	}

	// @Test
	public void test0() throws Exception {
		InterfaceMaker im = new InterfaceMaker();
		im.add(ServiceA.class);
		// im.add(m.getClass());
		im.add(ServiceB.class);

		Class type = im.create();

		System.out.println(type.getName());
		for (Method m1 : type.getMethods()) {
			System.out.println(m1.getName());
		}

		NettyServer server = new NettyServer(new ReflectResponder(type, mixAllImpls(new Class[] { ServiceA.class,
		        ServiceB.class }, new Object[] { new ServiceImplA(), new ServiceImplB() })),
		        new InetSocketAddress(9999));
		server.start();

		AbstractClientFactory<ServiceA> fac1 = AbstractClientFactory.createFactory(ServiceA.class);
		fac1.setUrl("netty://localhost:9999");
		ServiceA client = fac1.create();
		String ret = client.sayFromA("hello, laurence");
		System.out.println(ret);
		AbstractClientFactory<ServiceB> fac2 = AbstractClientFactory.createFactory(ServiceB.class);
		fac2.setUrl("netty://localhost:9999");
		ServiceB client2 = fac2.create();
		ret = client2.sayFromB("hello, laurence");
		System.out.println(ret);

		server.close();
	}

	// @Test
	public void test1() throws Exception {
		ServerBootstrap boot = new ServerBootstrap();
		boot.startUp(new Class[] { ServiceA.class, ServiceB.class }, new Object[] { new ServiceImplA(),
		        new ServiceImplB() }, 9999, "netty");

		AbstractClientFactory<ServiceA> fac = AbstractClientFactory.createFactory(ServiceA.class);
		fac.setUrl("netty://localhost:9999");
		ServiceA client = fac.create();
		String ret = client.sayFromA("hello, laurence");
		System.out.println(ret);
		AbstractClientFactory<ServiceB> fac2 = AbstractClientFactory.createFactory(ServiceB.class);
		fac2.setUrl("netty://localhost:9999");
		ServiceB client2 = fac2.create();
		ret = client2.sayFromB("hello, laurence");
		System.out.println(ret);

		boot.shutdown();
	}

	// @Test
	public void test2() {
		Class[] types = new Class[] { ServiceA.class, ServiceB.class };
		Object[] nn = new Object[] { new ServiceImplA(), new ServiceImplB() };
		Object impl = mixAllImpls(types, nn);
		System.out.println("!!! " + impl.getClass());
		String ss = ((ServiceA) (impl)).sayFromA("laurence");
		System.out.println(ss);
	}

	protected Object mixAllImpls(Class[] in, Object[] impls) {
		return Mixin.create(in, impls);
	}

	@Test
	public void test3() throws IOException {
		ServerBootstrap boot = new ServerBootstrap();
		boot.addService(ServiceA.class, new ServiceImplA());
		boot.addService(ServiceB.class, new ServiceImplB());
		boot.startUp(9999, "netty");

		AbstractClientFactory<ServiceA> fac = AbstractClientFactory.createFactory(ServiceA.class);
		fac.setUrl("netty://localhost:9999");
		ServiceA client = fac.create();
		String ret = client.sayFromA("hello, laurence");
		System.out.println(ret);
		AbstractClientFactory<ServiceB> fac2 = AbstractClientFactory.createFactory(ServiceB.class);
		fac2.setUrl("netty://localhost:9999");
		ServiceB client2 = fac2.create();
		ret = client2.sayFromB("hello, laurence");
		System.out.println(ret);

		boot.shutdown();
	}

}

interface ServiceA {
	public String sayFromA(String msg);
}

interface ServiceB {
	public String sayFromB(String msg);
}

class ServiceImplA implements ServiceA {

	@Override
	public String sayFromA(String msg) {
		System.out.println("From A: " + msg);
		return "From A: " + msg;
	}

}

class ServiceImplB implements ServiceB {

	@Override
	public String sayFromB(String msg) {
		System.out.println("From B: " + msg);
		return "From B: " + msg;
	}

}
