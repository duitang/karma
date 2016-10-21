package com.duitang.service.karma.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.http.Finder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HTTPServerTest {

	final static ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws Exception {
		MemoryCacheService mms = new MemoryCacheService();
		mms.memory_setString("aaaa", "bbbb", 5000);
		System.out.println("aaaa ---> " + mms.memory_getString("aaaa"));

		ServerBootstrap server = new ServerBootstrap();
		server.addService(IDemoService.class, mms);

		server.startUp(7788);

		Finder.enableHTTPService(8888);
		System.out.println(Finder.getHTTPServer(8888).getServiceProtocol());
		System.out.println(Finder.getHTTPServer(8888).getServiceURL());
		System.out.println(Finder.getHTTPServer(8888).getPort());
		String ret = null;
		Method m = null;

		m = IDemoService.class.getMethod("memory_getString", String.class);
		ret = invoke(IDemoService.class, m, new Object[] { "aaaa" });

		Assert.assertNotNull(ret);
		Assert.assertEquals(decodeReturnValue(ret, String.class), "bbbb");

		m = IDemoService.class.getMethod("memory_setString", String.class, String.class, int.class);
		ret = invoke(IDemoService.class, m, new Object[] { "aaaa", "cccc", 1234 });

		Assert.assertNotNull(ret);
		Assert.assertEquals(decodeReturnValue(ret, boolean.class), true);

		m = IDemoService.class.getMethod("memory_getString", String.class);
		ret = invoke(IDemoService.class, m, new Object[] { "aaaa" });

		Assert.assertNotNull(ret);
		Assert.assertEquals(decodeReturnValue(ret, String.class), "cccc");

		// parameter size error
		m = IDemoService.class.getMethod("memory_getString", String.class);
		ret = invoke(IDemoService.class, m, new Object[] { "aaaa", 12345 });
		System.out.println(ret);
		Assert.assertTrue(ret.contains("KarmaException"));

		try {

			// parameter size error
			m = IDemoService.class.getMethod("memory_setString", String.class);
			ret = invoke(IDemoService.class, m, new Object[] { "aaaa", "cccc", "1234" });
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Finder.disableHTTPService(8888);

		try {
			m = IDemoService.class.getMethod("memory_getString", String.class);
			ret = invoke(IDemoService.class, m, new Object[] { "aaaa" });
			System.out.println(ret);
			Assert.fail();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static String invoke(Class iface, Method m, Object[] param) throws Exception {
		// domain, method, param
		String pt = "http://localhost:8888/%s/%s?q=%s";

		String u = String.format(pt, iface.getName(), m.getName(), generateParam(Arrays.asList(param)));

		URL oracle = new URL(u);
		URLConnection yc = oracle.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		String inputLine;
		List<String> r0 = new ArrayList<String>();
		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			if (inputLine != null) {
				r0.add(inputLine);
			}
		}
		in.close();
		String r0s = StringUtils.join(r0);

		List<Map> ret = decodeReturn(r0s);
		System.out.println(ret);

		Assert.assertNotNull(ret);
		Assert.assertTrue(!ret.isEmpty());
		Map r1 = ret.get(0);
		if (r1.get("e") != null) {
			return (String) r1.get("e");
		}
		return (String) r1.get("r");

	}

	static String generateParam(List<Object> params) throws Exception {
		return mapper.writeValueAsString(params);
	}

	static List<Map> decodeReturn(Object r) throws Exception {
		return mapper.readValue(r.toString(), new TypeReference<List<Map>>() {
		});
	}

	static <T> T decodeReturnValue(Object r, Class<T> clz) throws Exception {
		return mapper.readValue(r.toString(), clz);
	}

}
