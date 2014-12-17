package com.duitang.service.base;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyTransceiver;
import org.junit.Test;

import com.duitang.service.demo.DemoService;

public class MetricalReflectRequestorTest {

	@Test
	public void test() throws Exception {
		NettyTransceiver trans = new NettyTransceiver(new InetSocketAddress(9999));
		DemoService cli = MetricalReflectRequestor.getClient(DemoService.class, trans);
		for (Method m : cli.getClass().getMethods()) {
			System.out.println(m.getName());
		}
		System.out.println(cli.toString());
	}

}
