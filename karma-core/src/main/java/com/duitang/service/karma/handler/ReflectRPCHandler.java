package com.duitang.service.karma.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.invoker.ReflectInvoker;
import com.duitang.service.karma.server.ServiceConfig;

public class ReflectRPCHandler extends TraceableRPCHandler {

	protected ServiceConfig conf;

	/**
	 * native
	 */
	protected Map<String, ReflectInvoker> services;

	public ServiceConfig getConf() {
		return conf;
	}

	public void setConf(ServiceConfig conf) {
		this.conf = conf;
	}

	public void init() throws KarmaException {
		services = new HashMap<String, ReflectInvoker>();
		for (Entry<Class, Object> en : conf.getServices().entrySet()) {
			String name = en.getKey().getName();
			ReflectInvoker impl = new ReflectInvoker(en.getKey(), en.getValue());
			services.put(name, impl);
		}
	}

	@Override
	protected void lookUp0(RPCContext ctx) throws KarmaException {
		ReflectInvoker ret = services.get(ctx.name);
		if (ret == null) {
			throw new KarmaException("domain[" + ctx.name + "] not found");
		}
		ctx.invoker = ret;
	}

	@Override
	protected void invoke0(RPCContext ctx) throws KarmaException {
		if (ctx.invoker == null){
			throw new KarmaException("method[" + ctx.method + "] not found");
		}
		ctx.ret = ctx.invoker.invoke(ctx.method, ctx.params);
	}

}
