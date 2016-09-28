package com.duitang.service.karma.router;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.meta.JsonPacket;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TraceContextHolder;

public class JsonRouter implements Router<JsonPacket> {

	protected String host;
	protected int port;
	protected RPCHandler handler;

	@Override
	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}

	@Override
	public void route(RPCContext ctx, JsonPacket ret) {
		TraceCell tc = ctx.tc;
		TraceContextHolder.push(tc);
		// tc.pid = InstanceTagHolder.INSTANCE_TAG.pid;
		ctx.tc = new TraceCell(false, host, port); // no leak
		ctx.tc.setIds(tc.traceId, tc.spanId);
		ctx.tc.sampled = tc.sampled;
		ctx.tc.isLocal = true;
		ctx.name = ret.getD();
		ctx.method = ret.getM();
		ctx.tc.name = ctx.method;
		ctx.tc.clazzName = ctx.name;
		ctx.params = new Object[] { ret.getP() };
		try {
			handler.lookUp(ctx);
			handler.invoke(ctx);
			ret.setR(ctx.ret);
		} catch (Throwable e) {
			ctx.ex = e;
			ret.setE(ExceptionUtils.getMessage(e) + "\n" + ExceptionUtils.getRootCauseMessage(e));
		} finally {
			tc.passivate(ctx.ex);
			TraceContextHolder.release();
			KarmaServerConfig.tracer.visit(tc);
		}
	}

	@Override
	public void setHostInfo(String host, int port) {
		this.host = host;
		this.port = port;
	}

}
