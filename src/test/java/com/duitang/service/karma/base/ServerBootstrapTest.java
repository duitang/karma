package com.duitang.service.karma.base;

import org.junit.Test;

import com.duitang.service.karma.demo.DemoJsonRPCImpl;
import com.duitang.service.karma.demo.DemoJsonRPCService;
import com.duitang.service.karma.demo.DemoService;
import com.duitang.service.karma.demo.MemoryCacheService;

public class ServerBootstrapTest {

	@Test
	public void test() throws Exception {
		MetricCenter.alterReportPeroid(30);
		MetricCenter.addLoggerReporterByName("karma");
		MetricCenter.enableConsoleReporter(true);
		ServerBootstrap boot = new ServerBootstrap();
		MemoryCacheService s1 = new MemoryCacheService();
		boot.addService(DemoService.class, s1);
		boot.addService(DemoJsonRPCService.class, new DemoJsonRPCImpl());
		s1.memory_setString("aaaa", "bbbb", 5000);
		System.out.println("aaaa ---> " + s1.memory_getString("aaaa"));

		boot.startUp(9999);
		Thread.sleep(10000000);
	}

}
