package com.duitang.service.karma.handler;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.invoker.IgnCaseInvoker;
import com.duitang.service.karma.invoker.ReflectInvoker;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JsonRPCHandler extends TraceableRPCHandler {

	final static protected ObjectMapper mapper = new ObjectMapper();

	protected Map<String, IgnCaseInvoker> lowercase;

	public JsonRPCHandler(ReflectRPCHandler handler) {
		this.lowercase = new HashMap<>();
		for (Entry<String, ReflectInvoker> en : handler.services.entrySet()) {
			this.lowercase.put(en.getKey().toLowerCase(), new IgnCaseInvoker(en.getValue()));
		}
	}

	@Override
	public void lookUp0(RPCContext ctx) throws KarmaException {
		String name = ctx.name.toLowerCase(Locale.ENGLISH);
		ctx.invoker = this.lowercase.get(name);
		if (ctx.invoker == null) {
			throw new KarmaException("domain not found: " + name);
		}
	}

	@Override
	public void invoke0(RPCContext ctx) throws KarmaException {
		Object[] params = null;
		try {
			String p = (String) ctx.params[0];
			List mp = mapper.readValue(p, ArrayList.class);
			params = new Object[mp.size()];
			Class[] ptypes = ctx.invoker.lookupParameterTypes(ctx.method.toLowerCase(Locale.ENGLISH));
			Class[][] rtypes = ctx.invoker.lookupParameterizedType(ctx.method.toLowerCase(Locale.ENGLISH));
			Object v;
			if (mp.size() != ptypes.length) {
				throw new KarmaException(
						ctx.name + "." + ctx.method + "(" + Arrays.toString(ctx.params) + ") parameter size error?");
			}
			for (int i = 0; i < mp.size(); i++) {
				v = mp.get(i);
				params[i] = castToParameter(v, ptypes[i], rtypes[i], 0);
			}
		} catch (Exception e) {
			throwIt(ctx.name + "." + ctx.method + "(" + Arrays.toString(ctx.params) + ") parameter convert error: ", e);
		}
		Object ret = null;
		try {
			ret = ctx.invoker.invoke(ctx.method, params);
		} catch (Exception e) {
			throwIt(ctx.name + "." + ctx.method + "(" + Arrays.toString(ctx.params) + ") invoke error:", e);
		}
		try {
			ctx.ret = mapper.writeValueAsString(ret);
		} catch (Exception e) {
			throwIt(ctx.name + "." + ctx.method + "(" + Arrays.toString(ctx.params) + ") return convert error:", e);
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
		if (target.equals(Date.class)) {
			return new Date(v.longValue());
		}
		return v;
	}

	protected Object castToParameter(Object v, Class tClz, Class[] pType, int depth) throws Exception {
		if (v == null) {
			return null;
		}
		if (tClz.isPrimitive()) {
			if (boolean.class.isAssignableFrom(tClz)) {
				return Boolean.valueOf(v.toString());
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
				HashMap<Object, Object> ret1 = new HashMap<>();
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
					throwIt("", e);
					throw e; // dummy
				}
			} else {
				ret = vv;
			}
		} else {
			throwIt("can't convert type " + v.getClass().getName() + " -> " + tClz.getName(),
					new KarmaException("JsonRPC error:"));
			throw new KarmaException(""); // dummy
		}
		return ret;
	}

	void throwIt(String msg, Throwable ex) throws KarmaException {
		if (KarmaException.class.isAssignableFrom(ex.getClass())) {
			throw (KarmaException) ex;
		}
		throw new KarmaException(msg, ExceptionUtils.getRootCause(ex));
	}
}
