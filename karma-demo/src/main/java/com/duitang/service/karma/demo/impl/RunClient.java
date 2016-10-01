/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import java.util.Arrays;
import java.util.List;

import com.duitang.service.karma.client.KarmaClient;
import com.duitang.service.karma.demo.ServiceG;
import com.duitang.service.karma.trace.Finder;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class RunClient {

	/**
	 * @param args
	 * @throws Throwable
	 * 			@throws
	 */
	public static void main(String[] args) throws Throwable {
		run(args[0], Integer.valueOf(args[1]), Long.valueOf(args[2]));
	}

	static void run(String param, int loop, long gap) throws Throwable {
		Finder.enableConsole(true);
		Finder.enableZipkin(null, "http://192.168.1.180:9411");
		// Finder.enableZipkin(null, "http://192.168.10.216:9411");
		String url = "localhost:" + RunService.ports.get(ServiceG.class);
		String[] u = new String[5];
		Arrays.fill(u, url);
		u[0] = "laurence:" + RunService.ports.get(ServiceG.class);
		List<String> nodes = Arrays.asList(u);
		KarmaClient<ServiceG> proxy = KarmaClient.createKarmaClient(ServiceG.class, nodes, "dev1");
		ServiceG cli = proxy.getService();
		for (int i = 0; i < loop; i++) {
			proxy.resetTrace();
			Thread.sleep(gap);
			System.out.println(cli.method_g(param));
		}
	}

}
