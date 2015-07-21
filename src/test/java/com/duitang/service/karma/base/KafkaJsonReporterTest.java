package com.duitang.service.karma.base;

import java.io.IOException;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import com.duitang.service.karma.demo.DemoService;
import com.duitang.service.karma.demo.MemoryCacheService;

public class KafkaJsonReporterTest {

	ServerBootstrap boot = null;
	ClientFactory<DemoService> fac = null;

	@Before
	public void setUp() throws Exception {
		MemoryCacheService impl = new MemoryCacheService();
		boot = new ServerBootstrap();
		try {
			// boot.startUp(DemoService.class, impl, 9090);
			boot.addService(DemoService.class, impl);
			boot.startUp(9090, "");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://127.0.0.1:9090");
	}

	@After
	public void tearDown() throws Exception {
		boot.shutdown();
	}

}
