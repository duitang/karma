/**
 * @author laurence
 * @since 2016年10月8日
 *
 */
package com.duitang.service.karma;

import java.util.Arrays;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.boot.ServerBootstrap;
import com.duitang.service.karma.client.KarmaClient;

/**
 * @author laurence
 * @since 2016年10月8日
 *
 */
public class TestOnceServerAndClient {

	static String KEY = "aaaa";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ServerBootstrap boot = new ServerBootstrap();
		MemoryCacheService s1 = new MemoryCacheService();
		boot.addService(IDemoService.class, s1);
		s1.memory_setString(KEY, "bbbb", 5000);
		System.out.println("aaaa ---> " + s1.memory_getString("aaaa"));

		boot.startUp(9999);

		Thread.sleep(2000);

		String url = "tcp://localhost:9999";

		KarmaClient<IDemoService> client = KarmaClient.createKarmaClient(IDemoService.class, Arrays.asList(url));

		String ret = client.getService().memory_getString(KEY);
		System.out.println("Karma Invoke Response: " + ret);

		Thread.sleep(500);

		KarmaClient.shutdownIOPool();
		// KarmaIoSession.shutdown();
		boot.shutdown();
	}

}
