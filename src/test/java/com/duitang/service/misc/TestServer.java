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
		        "localhost", 9999)), new ReflectData(type.getClassLoader()));

		r2.hello2();
		r1.hello();
	}
}

interface MyInter {
	public void hello();
}

interface YourInter {
	public void hello2();
}

class My implements MyInter, YourInter {
	public void hello() {
		System.out.println("hello: " + new Date());
	}

	public void hello2() {
		System.out.println("hello2: " + new Date());
	}
}
