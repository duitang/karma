package com.duitang.service.karma.support;

import com.duitang.service.karma.client.WRRBalancer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ProxiedWRRInvoker<T> {
  private final String name;
  private final Class<T> klass;
  private final Map<String, T> delegates;
  private final String fqdn;
  private final WRRBalancer balancer;

  private ProxiedWRRInvoker(String name, Class<T> klass, Map<String, T> delegates) {
    this.name = name;
    this.klass = klass;
    this.delegates = delegates;
    this.fqdn = klass.getCanonicalName() + '.' + name;
    this.balancer = WRRBalancer.getInstance(fqdn, Lists.newArrayList(delegates.keySet()));
  }

  public static <T> T newInstance(String name, Class<T> klass, List<T> delegates) {
    ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();
    for (int i = 0; i < delegates.size(); i++) {
      builder.put(String.valueOf(i), delegates.get(i));
    }
    ProxiedWRRInvoker<T> proxiedWRRInvoker = new ProxiedWRRInvoker<>(name, klass, builder.build());
    return proxiedWRRInvoker.newProxiedInvoker();
  }

  @SuppressWarnings("unchecked")
  private T newProxiedInvoker() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(klass);
    enhancer.setCallback(new MethodInterceptor() {
      @Override
      public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
          return methodProxy.invokeSuper(o, objects);
        }

        String next = balancer.next(null);
        T t = delegates.get(next);
        try {
          return methodProxy.invoke(t, objects);
        } catch (Throwable e) {
          balancer.fail(next);
          throw e;
        }
      }
    });
    return (T) enhancer.create();
  }

}
