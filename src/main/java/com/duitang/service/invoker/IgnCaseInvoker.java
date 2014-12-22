package com.duitang.service.invoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.duitang.service.KarmaException;

public class IgnCaseInvoker implements Invoker {

	protected ReflectInvoker proxy;
	protected Map<String, Method> proxyNoCase;
	protected Map<String, Class[]> typesNoCase;
	protected Map<String, Class[]> ptypesNoCase;

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
		this.ptypesNoCase = new HashMap<String, Class[]>();
		for (Entry<String, Class[]> en : invoker.paramTypes.entrySet()) {
			this.ptypesNoCase.put(en.getKey().toLowerCase(), en.getValue());
		}
	}

	@Override
	public Object invoke(String name, Object[] parameters) throws KarmaException {
		name = name.toLowerCase();
		Method m = proxyNoCase.get(name);
		return proxy.invokeMethod(m, parameters);
	}

	@Override
	public Class[] lookupParameterTypes(String name) throws KarmaException {
		name = name.toLowerCase();
		Class[] ret = typesNoCase.get(name);
		if (ret == null) {
			throw new KarmaException("Not found method: " + name);
		}
		return ret;
	}

	@Override
	public Class[] lookupParameterizedType(String name) throws KarmaException {
		name = name.toLowerCase();
		Class[] ret = ptypesNoCase.get(name);
		if (ret == null) {
			throw new KarmaException("Not found method: " + name);
		}
		return ret;
	}

}
