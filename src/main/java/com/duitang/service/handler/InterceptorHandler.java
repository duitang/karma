package com.duitang.service.handler;

import com.duitang.service.KarmaException;

public abstract class InterceptorHandler implements RPCHandler {

	protected RPCHandler proxy;

	public InterceptorHandler(RPCHandler handler) {
		this.proxy = handler;
	}

	@Override
	public void lookUp(RPCContext ctx) throws KarmaException {
		preLookUp(ctx);
		proxy.lookUp(ctx);
		postLookUp(ctx);
	}

	@Override
	public void invoke(RPCContext ctx) throws KarmaException {
		preInvoke(ctx);
		proxy.invoke(ctx);
		postInvoke(ctx);
	}

	protected abstract void preLookUp(RPCContext ctx);

	protected abstract void postLookUp(RPCContext ctx);

	protected abstract void preInvoke(RPCContext ctx);

	protected abstract void postInvoke(RPCContext ctx);

}
