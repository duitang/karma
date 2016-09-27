/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import java.util.Arrays;

import com.duitang.service.karma.boot.KarmaClientConfig;
import com.duitang.service.karma.client.KarmaClient;
import com.duitang.service.karma.demo.ServiceA;
import com.duitang.service.karma.trace.Finder;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class RunClient {

	/**
	 * @param args @throws Throwable @throws
	 */
	public static void main(String[] args) throws Throwable {
		run(args[0], Integer.valueOf(args[1]));
	}

	static void run(String param, int loop) throws Throwable {
		Finder.enableConsole(true);
		Finder.enableZipkin(null, "http://192.168.10.216:9411");
		String url = "localhost:" + RunService.ports.get(ServiceA.class);
		KarmaClient<ServiceA> proxy = KarmaClient.createKarmaClient(ServiceA.class, Arrays.asList(url), "dev1");
		ServiceA cli = proxy.getService();
		for (int i = 0; i < loop; i++) {
			proxy.resetTrace();
			System.out.println(cli.method_a(param));
		}
	}

}
