package com.duitang.service.karma.transport;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.meta.JsonPacket;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.trace.TraceCell;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonServlet extends AbstractHandler {

	final static protected String[] EMPTY = new String[0];
	static ObjectMapper mapper = new ObjectMapper();
	protected Router<JsonPacket> router;

	public void setRouter(Router<JsonPacket> router) {
		this.router = router;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
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
		// [domain, method]
		String[] dm = getDomainNameAndMethod(reqPath);
		if (dm.length < 1) {
			ret.setE("no domain or method specified");
			response.getWriter().println(mapper.writeValueAsString(ret));
			response.flushBuffer();
			return;
		}
		String pVal = request.getParameter("q");
		try {
			ret.setD(dm[0].toLowerCase());
			ret.setM(dm[1].toLowerCase());
			ret.setP(pVal);
			RPCContext ctx = new RPCContext();
			fillTrace(ctx, baseRequest, request);
			router.route(ctx, ret);
		} catch (KarmaException e) {
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

	public void fillTrace(RPCContext ctx, Request baseRequest, HttpServletRequest req) {
		String traceid = req.getHeader("traceid");
		String spanid = req.getHeader("spanid");
		Boolean sampled = Boolean.valueOf(req.getHeader("sampled"));
		Long tid = null;
		Long sid = null;
		if (traceid != null) {
			try {
				tid = Long.valueOf(traceid);
			} catch (Throwable t) {
				// ignore
			}
		}
		if (spanid != null) {
			try {
				sid = Long.valueOf(spanid);
			} catch (Throwable t) {
				// ignore
			}
		}
		String host = baseRequest.getHttpChannel().getEndPoint().getLocalAddress().getHostName();
		int port = baseRequest.getHttpChannel().getEndPoint().getLocalAddress().getPort();
		ctx.tc = new TraceCell(false, host, port);
		ctx.tc.setIds(tid, sid);
		ctx.tc.sampled = sampled;
		ctx.tc.name = "http:" + req.getPathInfo();
	}

}
