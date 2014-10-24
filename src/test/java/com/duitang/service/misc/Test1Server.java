package com.duitang.service.misc;

import java.net.InetSocketAddress;

import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.Opcodes;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.apache.avro.reflect.Nullable;
import org.apache.avro.reflect.ReflectData;

public class Test1Server {

	public static void main(String[] args) throws Exception {
		Class clz = genInterface(new Class[] { A.class, F.class });
		NettyServer server = new NettyServer(new ReflectResponder(clz, new MyImpl()), new InetSocketAddress(9999));
		server.start();

		A r1 = ReflectRequestor.getClient(A.class, new NettyTransceiver(new InetSocketAddress("localhost", 9999)),
		        new ReflectData(clz.getClassLoader()));
		F r2 = ReflectRequestor.getClient(F.class, new NettyTransceiver(new InetSocketAddress("localhost", 9999)),
		        new ReflectData());

		// System.out.println(r1.a("aaa"));
		System.out.println(r2.setbytes("aaa", "111".getBytes()));
		System.out.println(new String(r2.getbytes("aaa")));

	}

	static Class genInterface(Class[] clz) {
		final String[] interfaces = new String[clz.length];
		int i = 0;
		for (Class<?> interfac : clz) {
			interfaces[i] = interfac.getName().replace('.', '/');
			i++;
		}
		Class<?> klass = new ClassLoader(Test1Server.class.getClassLoader()) {
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

class MyImpl implements A, F {

	@Override
	public boolean setbytes(String name, byte[] data) {
		System.out.println(data);
		return false;
	}

	@Override
	public byte[] getbytes(String name) {
		System.out.println(name.getBytes());
		return name.getBytes();
	}

	@Override
	@Nullable
	public String a(String b) {
		return b;
	}

}