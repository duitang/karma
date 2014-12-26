package com.duitang.service.router;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.duitang.service.handler.RPCContext;
import com.duitang.service.handler.RPCHandler;
import com.duitang.service.meta.JsonPacket;

public class JsonRouter implements Router<JsonPacket> {

	protected RPCHandler handler;

	@Override
	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}

	@Override
	public void route(RPCContext ctx, JsonPacket ret) {
		ctx.name = ret.getD();
		ctx.method = ret.getM();
		ctx.params = new Object[] { ret.getP() };
		try {
			handler.lookUp(ctx);
			handler.invoke(ctx);
			ret.setR(ctx.ret);
		} catch (Throwable e) {
			ctx.ex = e;
			ret.setE(ExceptionUtils.getMessage(e) + "\n" + ExceptionUtils.getRootCauseMessage(e));
		}
	}

}
