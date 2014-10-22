package com.duitang.service.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import net.sf.cglib.asm.ClassWriter;
import net.sf.cglib.asm.Opcodes;

import org.apache.avro.reflect.Nullable;

public class TestAnnotation {
	public static void main(String[] args) throws Exception {
		for (Method m : D.class.getMethods()) {
			System.out.println(m.getAnnotation(Nullable.class));
		}

		Class[] clz = new Class[] { A.class };
		final String[] interfaces = new String[clz.length];
		int i = 0;
		for (Class<?> interfac : clz) {
			interfaces[i] = interfac.getName().replace('.', '/');
			i++;
		}

		Class<?> klass = new ClassLoader(TestAnnotation.class.getClassLoader()) {
			public Class<?> defineClass() {
				ClassWriter cw = new ClassWriter(0);
				cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE, "Foo", null,
				        "java/lang/Object", interfaces);
				// for (int i = 0; i < 3; i++) {
				// FieldVisitor fv = cw.visitField(0, "value" + i, "I", null,
				// null);
				// fv.visitAnnotation("Lorg/apache/avro/reflect/Nullable;",
				// true).visitEnd();
				// }
				cw.visitEnd();
				byte[] bytes = cw.toByteArray();
				return defineClass("Foo", bytes, 0, bytes.length);
			}
		}.defineClass();

		System.out.println(klass.getSimpleName());

		for (Field f : klass.getDeclaredFields()) {
			System.out.println(f + " " + Arrays.toString(f.getAnnotations()));
		}
	}

}

interface D extends A, B, C {

}