package com.duitang.service.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.duitang.service.KarmaException;
import com.duitang.service.invoker.IgnCaseInvoker;
import com.duitang.service.invoker.ReflectInvoker;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRPCHandler implements RPCHandler {

	static protected ObjectMapper mapper = new ObjectMapper();
	static protected ConcurrentHashMap<String, Class> types = new ConcurrentHashMap<String, Class>();

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

				if (clz.equals(v.getClass()) || clz.isAssignableFrom(v.getClass())) {
					params[i] = v;
				} else {
					params[i] = mapper.readValue((String) v, clz);
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

}
