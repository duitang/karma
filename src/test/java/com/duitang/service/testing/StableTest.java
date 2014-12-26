package com.duitang.service.testing;

import org.junit.Test;

import com.duitang.service.base.ClientFactory;
import com.duitang.service.demo.DemoService;

public class StableTest {

	/**
	 * remote exception behavier
	 */
	@Test
	public void test1() {
		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("localhost:9999");
		DemoService cli = fac.create();
		long ts = System.currentTimeMillis();
		try {
			ts = System.currentTimeMillis();
			cli.getError();
		} catch (Exception e) {
			System.out.println(e.getClass().getName());
			e.printStackTrace();
		}finally{
			ts = System.currentTimeMillis() - ts;			
		}
		System.out.println("time elapsed: " + ts + "ms");
		ts = System.currentTimeMillis();
		cli.getError();
		System.out.println("time elapsed: " + ts + "ms");
		System.out.println(cli.memory_setString("aaa", "bbb", 5000));
		System.out.println(cli.memory_getString("aaa"));
		fac.release(cli);
	}

}
