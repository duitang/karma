package com.duitang.service.misc;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.Opcodes;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.apache.avro.reflect.Nullable;
import org.apache.avro.reflect.ReflectData;

import com.duitang.service.codecs.MapUtils;

public class TestServer {

	public static void main(String[] args) throws Exception {
		Class clz = genInterface(new Class[] { A.class, B.class, C.class });
		System.out.println(clz.getName());
		for (Method m : clz.getMethods()) {
			System.out.println(m.getAnnotation(Nullable.class));
		}

		NettyServer server = new NettyServer(new ReflectResponder(clz, new Impl()), new InetSocketAddress(9999));
		server.start();

		A r1 = ReflectRequestor.getClient(A.class, new NettyTransceiver(new InetSocketAddress("localhost", 9999)),
		        new ReflectData(clz.getClassLoader()));
		B r2 = ReflectRequestor.getClient(B.class, new NettyTransceiver(new InetSocketAddress("localhost", 9999)),
		        new ReflectData());
		C r3 = ReflectRequestor.getClient(C.class, new NettyTransceiver(new InetSocketAddress("localhost", 9999)),
		        new ReflectData());

		HashMap m = new HashMap();
		m.put("111", "222");
		m.put("bbb", 333);

		// System.out.println(r2.hello3("11", 1, m).mp);
		// ByteBuffer src;
		System.out.println(r3.c("11"));
		// System.out.println(MapUtils.bytesToObject(src, HashMap.class));
		// System.out.println(r2.hello2());
		// System.out.println(r1.hello("shit"));
		// System.out.println(r1.hello(null));
		System.out.println(r1.a("aaa"));
		System.out.println(r2.b(null));
		System.out.println(r3.c("ccc"));
	}

	static Class genInterface(Class[] clz) {
		final String[] interfaces = new String[clz.length];
		int i = 0;
		for (Class<?> interfac : clz) {
			interfaces[i] = interfac.getName().replace('.', '/');
			i++;
		}
		Class<?> klass = new ClassLoader(TestServer.class.getClassLoader()) {
			public Class<?> defineClass() {
				ClassWriter cw = new ClassWriter(0);
				cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE,
				        "com/duitang/service/misc/FacadeService", null, "java/lang/Object", interfaces);
				cw.visitEnd();
				byte[] bytes = cw.toByteArray();
				return defineClass("com.duitang.service.misc.FacadeService", bytes, 0, bytes.length);
			}
		}.defineClass();
		return klass;
	}
}

class MyObj {
	String name;
	int id;
	@Nullable
	ByteBuffer mp;
	HashMap src;
}

interface MyInter {
	@Nullable
	public String hello(@Nullable String msg);
}

interface YourInter {

	// @AvroSchema("{\"type\":\"map\",\"values\":\"string\",\"default\":null}")
	public String hello2();

	public MyObj hello3(String name, int id, HashMap pp);
}

class My implements MyInter, YourInter {

	@Override
	public String hello(String msg) {
		String ret = msg + " hello: " + new Date();
		System.out.println(ret);
		return ret;
	}

	@Override
	// @AvroSchema("{\"type\":\"map\",\"values\":\"string\",\"default\":null}")
	public String hello2() {
		String ret1 = "hello2: " + new Date();
		System.out.println(ret1);
		Map<String, String> ret = new HashMap<>();
		ret.put("aaa", ret1);
		return MapUtils.objectToJson(ret);
	}

	@Override
	public MyObj hello3(String name, int id, HashMap pp) {
		System.out.println(pp);
		System.out.println("ending...");
		MyObj ret = new MyObj();
		ret.id = id;
		ret.name = name;
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("aaa", 2.1d);
		data.put("bbb", "ccc");
		ret.mp = MapUtils.objectToBytes(data);
		ret.src = (HashMap) data;
		return ret;
	}

}

class Impl implements A, B, C {

	@Override
	@Nullable
	public HashMap c(String b) {
		HashMap ret = new HashMap();
		ret.put("999", "111");
		ret.put("888", "ccc");
		return ret;
	}

	@Override
	@Nullable
	public String b(String b) {
		return b + "--->b";
	}

	@Override
	@Nullable
	public String a(String b) {
		return b + "--->a";
	}

}
