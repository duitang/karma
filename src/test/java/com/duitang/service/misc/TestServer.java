package com.duitang.service.misc;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;

import net.sf.cglib.proxy.InterfaceMaker;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.apache.avro.reflect.ReflectData;

public class TestServer {

	public static void main(String[] args) throws Exception {
		// My m = new My();
		InterfaceMaker im = new InterfaceMaker();
		im.add(YourInter.class);
		// im.add(m.getClass());
		im.add(MyInter.class);

		Class type = im.create();

		System.out.println(type.getName());
		for (Method m1 : type.getMethods()) {
			System.out.println(m1.getName());
		}

		NettyServer server = new NettyServer(new ReflectResponder(type, new My()), new InetSocketAddress(9999));
		server.start();

		MyInter r1 = ReflectRequestor.getClient(MyInter.class, new NettyTransceiver(new InetSocketAddress("localhost",
		        9999)), new ReflectData(type.getClassLoader()));
		YourInter r2 = ReflectRequestor.getClient(YourInter.class, new NettyTransceiver(new InetSocketAddress(
		        "localhost", 9999)), new ReflectData());

		r2.hello2();
		r1.hello();
	}
}

interface MyInter {
	public String hello();
}

interface YourInter {
	public String hello2();
}

class My implements MyInter, YourInter {
	public String hello() {
		String ret = "hello: " + new Date();
		System.out.println(ret);
		return ret;
	}

	public String hello2() {
		String ret = "hello2: " + new Date();
		System.out.println(ret);
		return ret;
	}
}
