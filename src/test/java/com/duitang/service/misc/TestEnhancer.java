package com.duitang.service.misc;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.junit.Test;

public class TestEnhancer {

	// @Test
	public void test1() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(SampleClass.class);
		enhancer.setCallback(new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				System.out.println(method.getName());
				return proxy.invokeSuper(obj, args);
			}
		});
		SampleClass proxy = (SampleClass) enhancer.create();
		proxy.test("aaa");

	}

	// @Test
	public void test2() {
		SampleClass aa = new SampleClass();
		Sample a = createWrapper(aa);
		System.out.println(a.test("aaa"));

	}

	@Test
	public void test3() {
		String ss = "11";
		CharSequence ret = ca(CharSequence.class, ss);
		System.out.println(ret);
	}

	<T> T ca(Class<T> cls, Object o) {
		return cls.cast(o);
	}

	public static Sample createWrapper(final Sample source) {

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(SampleClass.class);
		enhancer.setCallback(new MethodInterceptor() {

			public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy)
			        throws Throwable {

				System.out.println("invoked: " + method.getName());

				return methodProxy.invoke(source, args);
			}
		});

		return (Sample) enhancer.create();
	}

}

interface Sample {
	public String test(String input);
}

class SampleClass implements Sample {
	public String test(String input) {
		return "Hello world!";
	}
}

class Foobar implements MethodInterceptor {

	public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		System.out.println(method.getName());
		return methodProxy.invokeSuper(object, args);
	}
}
