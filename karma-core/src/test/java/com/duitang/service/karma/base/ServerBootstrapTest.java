package com.duitang.service.karma.base;

import org.junit.Test;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.karma.boot.ServerBootstrap;

public class ServerBootstrapTest {

	@Test
	public void test() throws Exception {
		ServerBootstrap boot = new ServerBootstrap();
		MemoryCacheService s1 = new MemoryCacheService();
		boot.addService(IDemoService.class, s1);
		s1.memory_setString("aaaa", "bbbb", 5000);
		System.out.println("aaaa ---> " + s1.memory_getString("aaaa"));

		boot.startUp(9999);
		Thread.sleep(10 * 1000);
	}

}
