package com.duitang.service.karma.invoker;

import com.duitang.service.karma.KarmaException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class IgnCaseInvoker implements Invoker {

  protected ReflectInvoker proxy;
  protected Map<String, Method> proxyNoCase;
  protected Map<String, Class[]> typesNoCase;
  protected Map<String, Class[][]> ptypesNoCase;

  public IgnCaseInvoker(ReflectInvoker invoker) {
    this.proxy = invoker;
    this.proxyNoCase = new HashMap<String, Method>();
    for (Entry<String, Method> en : invoker.proxy1.entrySet()) {
      this.proxyNoCase.put(en.getKey().toLowerCase(), en.getValue());
    }
    this.typesNoCase = new HashMap<String, Class[]>();
    for (Entry<String, Class[]> en : invoker.types.entrySet()) {
      this.typesNoCase.put(en.getKey().toLowerCase(), en.getValue());
    }
    this.ptypesNoCase = new HashMap<String, Class[][]>();
    for (Entry<String, Class[][]> en : invoker.paramTypes.entrySet()) {
      this.ptypesNoCase.put(en.getKey().toLowerCase(), en.getValue());
    }
  }

  @Override
  public Object invoke(String name, Object[] parameters) throws KarmaException {
    Method m = proxyNoCase.get(name.toLowerCase(Locale.ENGLISH));
    return proxy.invokeMethod(m, parameters);
  }

  @Override
  public Class[] lookupParameterTypes(String name) throws KarmaException {
    Class[] ret = typesNoCase.get(name.toLowerCase(Locale.ENGLISH));
    if (ret == null) {
      throw new KarmaException("Not found method: " + name);
    }
    return ret;
  }

  @Override
  public Class[][] lookupParameterizedType(String name) throws KarmaException {
    Class[][] ret = ptypesNoCase.get(name.toLowerCase(Locale.ENGLISH));
    if (ret == null) {
      throw new KarmaException("Not found method: " + name);
    }
    return ret;
  }

}
