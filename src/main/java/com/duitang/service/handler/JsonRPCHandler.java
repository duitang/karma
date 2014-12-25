package com.duitang.service.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;

import com.duitang.service.KarmaException;
import com.duitang.service.invoker.IgnCaseInvoker;
import com.duitang.service.invoker.ReflectInvoker;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRPCHandler implements RPCHandler {

	final static protected ObjectMapper mapper = new ObjectMapper();

	protected Map<String, IgnCaseInvoker> lowercase;

	public JsonRPCHandler(ReflectRPCHandler handler) {
		this.lowercase = new HashMap<String, IgnCaseInvoker>();
		for (Entry<String, ReflectInvoker> en : handler.services.entrySet()) {
			this.lowercase.put(en.getKey().toLowerCase(), new IgnCaseInvoker(en.getValue()));
		}
	}

	@Override
	public void lookUp(RPCContext ctx) throws KarmaException {
		String name = ctx.name.toLowerCase(Locale.ENGLISH);
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
			Class[] ptypes = ctx.invoker.lookupParameterTypes(ctx.method.toLowerCase(Locale.ENGLISH));
			Class[][] rtypes = ctx.invoker.lookupParameterizedType(ctx.method.toLowerCase(Locale.ENGLISH));
			Object v;
			for (int i = 0; i < mp.size(); i++) {
				v = mp.get(i);
				params[i] = castToParameter(v, ptypes[i], rtypes[i], 0);
			}
			Object ret = ctx.invoker.invoke(ctx.method, params);
			ctx.ret = mapper.writeValueAsString(ret);
		} catch (Exception e) {
			throw new KarmaException(ctx.name + "." + ctx.method + "(" + Arrays.toString(ctx.params) + ") error:", e);
		}

	}

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

	protected Object castToParameter(Object v, Class tClz, Class[] pType, int depth) throws Exception {
		if (v == null) {
			return null;
		}
		if (tClz.isPrimitive()) {
			if (boolean.class.isAssignableFrom(tClz)) {
				return Boolean.valueOf(v.toString()).booleanValue();
			} else {
				return getNumberValue(0, tClz);
			}
		}
		Object ret;
		if (tClz.isAssignableFrom(v.getClass())) {
			// FIXME: check if collection and array, ignore map
			if (ClassUtils.isAssignable(v.getClass(), Collection.class)) {
				Collection vv = (Collection) v;
				if (pType.length > 0) {
					ret = vv.getClass().newInstance();
					for (Object vvv : vv) {
						((Collection) ret).add(castToParameter(vvv, pType[0], null, depth + 1));
					}
				} else {
					ret = vv; // no generic type found, just return it
				}
			} else if (v instanceof Map) {
				// FIXME check map key/value generic parameter type
				Set<Entry> mm = ((Map) v).entrySet();
				HashMap ret1 = new HashMap();
				Object retK, retV;
				for (Entry en : mm) {
					retK = mapper.convertValue(en.getKey(), pType[0]);
					retV = mapper.convertValue(en.getValue(), pType[1]);
					ret1.put(retK, retV);
				}
				ret = ret1;
			} else {
				ret = v;
			}
		} else if (v instanceof Number) {
			ret = getNumberValue((Number) v, tClz);
		} else if (v instanceof Map) {
			Map mm = (Map) v;
			ret = mapper.convertValue(mm, tClz);
		} else if (v instanceof Collection) {
			Collection vv = (Collection) v;
			if (pType.length > 0) {
				try {
					ret = vv.getClass().newInstance();
					for (Object vvv : vv) {
						((Collection) ret).add(castToParameter(vvv, pType[0], null, depth + 1));
					}
				} catch (Exception e) {
					throw new KarmaException(e);
				}
			} else {
				ret = vv;
			}
		} else {
			throw new KarmaException("can't convert type " + v.getClass().getName() + " -> " + tClz.getName());
		}
		return ret;
	}
}
