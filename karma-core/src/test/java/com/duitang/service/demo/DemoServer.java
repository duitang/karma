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
	final static ServerBootstrap boot = new ServerBootstrap();
	final static MemoryCacheService s1 = new MemoryCacheService();
	static int port = 9999;

	static public void startUp() throws Exception {
		if (!boot.isOnline()) {
			boot.addService(IDemoService.class, new MemoryCacheService());
			boot.startUp(port);
		}
	}

	static public void shutdown() {
		try {
			if (boot.isOnline()) {
				boot.shutdown();
			}
		} catch (Exception e) {
			//
		}
	}

	public int getPort() {
		return port;
	}

}
