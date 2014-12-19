package com.duitang.service.router;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.duitang.service.KarmaException;
import com.duitang.service.handler.RPCContext;
import com.duitang.service.handler.RPCHandler;
import com.duitang.service.meta.JsonPacket;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRouter extends AbstractHandler {

	static protected String[] EMPTY = new String[0];

	static ObjectMapper mapper = new ObjectMapper();

	protected RPCHandler handler;

	public RPCHandler getHandler() {
		return handler;
	}

	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		JsonPacket ret = new JsonPacket();
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		// FIXME: check if POST method here
		// String type = request.getMethod();
		String reqPath = request.getPathInfo();
		if (reqPath.endsWith("/")) {
			ret.setE("no domain or method specified");
			response.getWriter().println(mapper.writeValueAsString(ret));
			response.flushBuffer();
			return;
		}
		String[] dm = getDomainNameAndMethod(reqPath);
		if (dm.length < 1) {
			ret.setE("no domain or method specified");
			response.getWriter().println(mapper.writeValueAsString(ret));
			response.flushBuffer();
			return;
		}
		String pVal = request.getParameter("q");
		RPCContext ctx = new RPCContext(dm[0].toLowerCase(), dm[1].toLowerCase(), new Object[] { pVal });
		ret.setD(ctx.name);
		ret.setM(ctx.method);
		ret.setP(pVal);
		try {
			handler.lookUp(ctx);
			handler.invoke(ctx);
			ret.setR(ctx.ret);
		} catch (KarmaException e) {
			ctx.ex = e;
			ret.setE(e.getMessage());
		}
		response.getWriter().println(mapper.writeValueAsString(ret));
		response.flushBuffer();
	}

	protected String[] getDomainNameAndMethod(String path) {
		if (path == null) {
			return EMPTY;
		}
		int pos = path.lastIndexOf('/');
		String[] ret = new String[2];
		if (pos > 0) {
			ret[0] = path.substring(1, pos).replace('/', '.');
			ret[1] = path.substring(pos + 1);
			return ret;
		}
		return EMPTY;
	}

}
