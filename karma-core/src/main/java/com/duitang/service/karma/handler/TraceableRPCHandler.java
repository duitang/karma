/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.handler;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.KarmaServerConfig;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public abstract class TraceableRPCHandler implements RPCHandler {

	protected abstract void lookUp0(RPCContext ctx) throws KarmaException;

	protected abstract void invoke0(RPCContext ctx) throws KarmaException;

	@Override
	public void lookUp(RPCContext ctx) throws KarmaException {
		if (ctx.tc != null) {
			ctx.tc.name = ctx.method;
			ctx.tc.clazzName = ctx.name;
			ctx.tc.active();
		}
		lookUp0(ctx);
	}

	@Override
	public void invoke(RPCContext ctx) throws KarmaException {
		Throwable t0 = null;
		try {
			invoke0(ctx);
		} catch (KarmaException e) {
			t0 = e;
			throw e;
		} finally {
			if (ctx.tc != null) {
				ctx.tc.passivate(t0);
				KarmaServerConfig.tracer.visit(ctx.tc);
			}
		}
	}

}
