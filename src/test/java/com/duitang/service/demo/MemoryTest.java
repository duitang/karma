package com.duitang.service.demo;

import org.junit.Test;

import com.duitang.service.base.ClientFactory;

public class MemoryTest {

	@Test
	public void test1() {
		DemoService cli = null;
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://" + "localhost" + ":" + 9999);

		cli = fac.create();
		boolean r = cli.memory_setString("aaa", "bbb", 50000);
		System.out.println(r);

		fac.release(cli);
	}

}
