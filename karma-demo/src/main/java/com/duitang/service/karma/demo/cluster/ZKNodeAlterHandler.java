/**
 * @author laurence
 * @since 2016年10月27日
 *
 */
package com.duitang.service.karma.demo.cluster;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author laurence
 * @since 2016年10月27日
 *
 */
public class ZKNodeAlterHandler extends AbstractHandler {

	protected Object proxy;
	protected Map<String, Method> entries;

	public ZKNodeAlterHandler(Object px) {
		proxy = px;
		entries = new HashMap<>();
		for (Method m : px.getClass().getDeclaredMethods()) {
			entries.put(m.getName(), m);
		}
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String cmd = target.substring(1);
		Object[] params = parseParameters(request.getQueryString());
		String result = gateway(cmd, params);
		response.setContentType("text/html; charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(result);
		baseRequest.setHandled(true);
	}

	static Object[] parseParameters(String qry) {
		List<Object> ret = new ArrayList();
		String[] sss = qry.split("&");
		for (String ss : sss) {
			String[] s = ss.split("=");
			ret.add(s[1]);
		}
		return ret.toArray(new Object[ret.size()]);
	}

	public String gateway(String cmd, Object[] params) {
		Object r0 = null;
		if (entries.containsKey(cmd)) {
			try {
				r0 = entries.get(cmd).invoke(proxy, params);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		String ret = "";
		if (r0 != null) {
			ret = "<h1>" + r0 + "</h1>";
		} else {
			ret = "<h1>function &lt" + cmd + "&gt not found</h1>";
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
//		class Foobar {
//			@SuppressWarnings("unused")
//			public String hello(String msg) {
//				return "hello," + msg + "," + new Date();
//			}
//		}
//		Object foo = new Foobar();
//		Server server = new Server(8080);
//		server.setHandler(new ZKNodeAlterHandler(foo));
//		server.start();
//		server.join();

		String s = "{\"begin\": true, \"ok\": 0.99, \"lost\": false, \"u\": 1, \"end\": false, \"id\": 1, \"mean\": 200}";
		ObjectMapper mapper = new ObjectMapper();
		ModifyItem obj = mapper.readValue(s, ModifyItem.class);
		System.out.println(obj);
	}
}
