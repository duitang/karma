/**
 * @author laurence
 * @since 2016年10月19日
 *
 */
package com.duitang.service.demo;

import com.duitang.service.karma.boot.ServerBootstrap;

/**
 * @author laurence
 * @since 2016年10月19日
 *
 */
public class DemoServer {

	final static String KEY = "aaaa";
	final static String VAL = "bbbb";
	ServerBootstrap boot = new ServerBootstrap();
	MemoryCacheService s1 = new MemoryCacheService();
	int port = 9999;

	public DemoServer(int port) throws Exception {
		boot.addService(IDemoService.class, s1);
		s1.memory_setString(KEY, VAL, 5000);
		System.out.println("aaaa ---> " + s1.memory_getString(KEY));

		this.port = port;
		boot.startUp(port);
	}

	public void shutdown() {
		boot.shutdown();
	}

	public int getPort() {
		return this.port;
	}

}
