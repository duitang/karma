package com.duitang.service.router;

import com.duitang.service.KarmaException;
import com.duitang.service.handler.RPCContext;
import com.duitang.service.handler.RPCHandler;
import com.duitang.service.meta.JsonPacket;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRouter implements Router<JsonPacket> {

	static ObjectMapper mapper = new ObjectMapper();
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
		} catch (KarmaException e) {
			ctx.ex = e;
			ret.setE(e.getMessage());
		}
	}

}
