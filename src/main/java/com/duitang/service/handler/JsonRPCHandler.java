package com.duitang.service.handler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ClassUtils;

import com.duitang.service.KarmaException;
import com.duitang.service.invoker.IgnCaseInvoker;
import com.duitang.service.invoker.ReflectInvoker;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRPCHandler implements RPCHandler {

	static protected ObjectMapper mapper = new ObjectMapper();
	static protected ConcurrentHashMap<String, Class> types = new ConcurrentHashMap<String, Class>();
	static protected Map<String, Class> rawTypes = new HashMap<String, Class>();

	static {
		rawTypes.put(int[].class.getName(), int.class);
		rawTypes.put(Integer[].class.getName(), Integer.class);
		rawTypes.put(long[].class.getName(), long.class);
		rawTypes.put(Long[].class.getName(), Long.class);
		rawTypes.put(float[].class.getName(), float.class);
		rawTypes.put(Float[].class.getName(), Float.class);
		rawTypes.put(double[].class.getName(), double.class);
		rawTypes.put(Double[].class.getName(), Double.class);
		rawTypes.put(short[].class.getName(), short.class);
		rawTypes.put(Short[].class.getName(), Short.class);
		rawTypes.put(boolean[].class.getName(), boolean.class);
		rawTypes.put(Boolean[].class.getName(), Boolean.class);
		rawTypes.put(byte[].class.getName(), byte.class);
		rawTypes.put(Byte[].class.getName(), Byte.class);
		rawTypes.put(char[].class.getName(), char.class);
		rawTypes.put(Character[].class.getName(), Character.class);
	}

	protected ReflectRPCHandler handler;
	protected Map<String, IgnCaseInvoker> lowercase;

	public JsonRPCHandler(ReflectRPCHandler handler) {
		this.handler = handler;
		this.lowercase = new HashMap<String, IgnCaseInvoker>();
		for (Entry<String, ReflectInvoker> en : handler.services.entrySet()) {
			this.lowercase.put(en.getKey().toLowerCase(), new IgnCaseInvoker(en.getValue()));
		}

	}

	@Override
	public void lookUp(RPCContext ctx) throws KarmaException {
		String name = ctx.name.toLowerCase();
		ctx.invoker = this.lowercase.get(name);
		if (ctx.invoker == null) {
			throw new KarmaException("domain not found");
		}
	}

	@Override
	public void invoke(RPCContext ctx) throws KarmaException {
		try {
			String p = (String) ctx.params[0];
			List mp = mapper.readValue(p, ArrayList.class);
			Object[] params = new Object[mp.size()];
			Class[] ptypes = ctx.invoker.lookupParameterTypes(ctx.method.toLowerCase());
			Map mm = null;
			String t;
			Object v;
			Class clz;
			for (int i = 0; i < mp.size(); i++) {
				mm = (Map) mp.get(i);
				t = (String) mm.get("t");
				if (t == null) {
					clz = ptypes[i];
				} else {
					clz = types.get(t);
					if (clz == null) {
						try {
							types.putIfAbsent(t, Class.forName(t));
						} catch (RuntimeException e) {
							throw new KarmaException(e);
						}
						clz = types.get(t);
					}
				}

				v = mm.get("v");
				if (v instanceof Number) {
					v = getNumberValue((Number) v, clz);
				}

				if (v instanceof Collection) {
					if (clz.isArray()) { // array
						Class tp = clz.getComponentType();
						Object dest = Array.newInstance(tp, ((Collection) v).size());
						Object[] src = ((Collection) v).toArray();
						for (int ii = 0; ii < src.length; ii++) {
							Array.set(dest, ii, toTypeValue(src[ii], tp));
						}
						params[i] = dest;
					} else if (Collection.class.isAssignableFrom(clz)) { // collection
						Class[] ts = ctx.invoker.lookupParameterizedType(ctx.method.toLowerCase());
						if (ts[i] != null) {
							// only 1 parameter type support!!!
							Collection retCol = (Collection) clz.newInstance();
							Collection vv = (Collection) v;
							for (Object obj : vv) {
								retCol.add(toTypeValue(obj, ts[i]));
							}
							params[i] = retCol;
						} else {
							throw new KarmaException("d) not supported type: " + v.getClass().getName() + " && " + clz.getName());
						}
					} else if (Map.class.isAssignableFrom(clz)) {// map
					} else { // ???
						throw new KarmaException("a) not supported type: " + v.getClass().getName() + " && " + clz.getName());
					}
				} else if (clz.equals(v.getClass()) || clz.isAssignableFrom(v.getClass())) {
					params[i] = v;
				} else if (v instanceof String) {
					params[i] = mapper.readValue((String) v, clz);
				} else {
					throw new KarmaException("b) not supported type: " + v.getClass().getName() + " && " + clz.getName());
				}
			}
			Object ret = ctx.invoker.invoke(ctx.method, params);
			ctx.ret = mapper.writeValueAsString(ret);
		} catch (Exception e) {
			throw new KarmaException(e);
		}

	}

	/**
	 * FIXME: dirty code
	 * 
	 * @param val
	 * @param target
	 * @return
	 */
	protected Object getNumberValue(Number v, Class target) {
		if (target.equals(Integer.class)) {
			return v.intValue();
		}
		if (target.equals(Long.class)) {
			return v.longValue();
		}
		if (target.equals(Double.class)) {
			return v.doubleValue();
		}
		if (target.equals(Float.class)) {
			return v.floatValue();
		}
		if (target.equals(Byte.class)) {
			return v.byteValue();
		}
		if (target.equals(Short.class)) {
			return v.shortValue();
		}
		return v;
	}

	protected Object toTypeValue(Object src, Class toClz) throws KarmaException {
		if (toClz.isPrimitive()) {
			toClz = ClassUtils.primitiveToWrapper(toClz);
		}

		if (src == null) {
			return src;
		}
		if (toClz.isAssignableFrom(src.getClass())) {
			return src;
		}
		if (Number.class.isAssignableFrom(toClz) && src instanceof Number) {
			return getNumberValue((Number) src, toClz);
		}
		if (src instanceof String) {
			try {
				return mapper.readValue((String) src, toClz);
			} catch (Exception e) {
			}
		}
		throw new KarmaException("c) not supported type: " + src.getClass().getName() + " && " + toClz.getName());
	}
}
