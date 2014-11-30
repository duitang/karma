package com.duitang.service.misc;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.junit.Test;

/**
 * 
 * @author shaman
 */
public class CglibTest {

	public static class Foo {

	}

	private static class MethodInterceptorImpl implements MethodInterceptor {

		public MethodInterceptorImpl() {
		}

		@Override
		public Object intercept(Object o, Method method, Object[] os, MethodProxy mp) throws Throwable {

			if (method.getName().equals("finalize")) {
				System.err.println("FINALIZING!");
			}

			return mp.invokeSuper(o, os);
		}
	}

	@Test
	public void testEnhance() {

		Enhancer enhancer = new Enhancer();

		Callback callback = new MethodInterceptorImpl();

		Foo created = (Foo) enhancer.create(Foo.class, callback);

		created = null;

		System.gc();
	}
}
