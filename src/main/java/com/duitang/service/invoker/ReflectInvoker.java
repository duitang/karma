package com.duitang.service.invoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.duitang.service.KarmaException;

public class ReflectInvoker implements Invoker {

	protected Class iface;
	protected Object impl;

	// direct name lookup
	protected Map<String, Method> proxy1;

	// parameter types
	protected Map<String, Class[]> types;

	// name + parameter lookup
	protected Map<String, Method> proxy2;

	public ReflectInvoker(Class iface, Object impl) throws KarmaException {
		this.iface = iface;
		this.impl = impl;
		init();
	}

	protected void init() {
		proxy1 = new HashMap<String, Method>();
		types = new HashMap<String, Class[]>();
		Method[] iface_methods = iface.getMethods();
		for (Method m : iface_methods) {
			String name = m.getName();
			m.setAccessible(true);
			// FIXME: check no name duplication here
			proxy1.put(name, m);
			Class[] oldtt = m.getParameterTypes();
			Class[] newtt = new Class[oldtt.length];
			for (int i = 0; i < oldtt.length; i++) {
				if (oldtt[i].isPrimitive()) {
					newtt[i] = getBigType(oldtt[i]);
					continue;
				}
				newtt[i] = oldtt[i];
			}
			types.put(name, newtt);
		}

		// FIXME: name + parameter support next time
		proxy2 = new HashMap<String, Method>();
	}

	@Override
	public Object invoke(String name, Object[] parameters) throws KarmaException {
		Method m = getMethod(name);
		return invokeMethod(m, parameters);
	}

	@Override
	public Class[] lookupParameterTypes(String name) throws KarmaException {
		if (!types.containsKey(name)) {
			throw new KarmaException("Not found method: " + name);
		}
		return types.get(name);
	}

	protected Method getMethod(String name) throws KarmaException {
		Method ret = proxy1.get(name);
		if (ret == null) {
			// FIXME: add name + parameter support next time
		}
		if (ret == null) {
			throw new KarmaException("not found method: " + name);
		}
		return ret;
	}

	protected Object invokeMethod(Method m, Object[] parameters) throws KarmaException {
		Object ret;
		try {
			ret = m.invoke(impl, parameters);
		} catch (Exception e) {
			throw new KarmaException(e);
		}
		return ret;
	}

	protected Class getBigType(Class t) {
		if (t == int.class) {
			return Integer.class;
		}
		if (t == float.class) {
			return Float.class;
		}
		if (t == double.class) {
			return Double.class;
		}
		if (t == long.class) {
			return Long.class;
		}
		if (t == short.class) {
			return Short.class;
		}
		if (t == byte.class) {
			return Byte.class;
		}
		if (t == boolean.class) {
			return Boolean.class;
		}
		return t;
	}

}
