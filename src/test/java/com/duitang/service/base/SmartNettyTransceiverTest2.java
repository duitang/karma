package com.duitang.service.base;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.ipc.PackageTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.avro.ipc.NettyTransceiver;

import com.duitang.service.demo.DemoService;

public class SmartNettyTransceiverTest2 {

	static String data;
	static int loop = 1000000;

	@Before
	public void setUp() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 40000; i++) {
			sb.append("a");
		}
		data = sb.toString();
	}

	@After
	public void tearDown() throws Exception {
	}

	 @Test
	public void testOne() throws Exception {
//		Class<DemoService> clz = DemoService.class;
//		String name = "memory_setString";
//		Object param = new Object[] { "aaa", data, 50000 };
//		for (int i = 0; i < 15; i++) {
//			List<ByteBuffer> data1 = PackageTester.mock(clz, name, param);
//			SmartNettyTransceiver tran = new SmartNettyTransceiver(new InetSocketAddress("localhost", 9999));
//			tran.transceive(data1);
//		}
	}

//	@Test
	public void testN() throws Exception {
		Thread[] testers = new Thread[30];
		for (int i = 0; i < testers.length; i++) {
			testers[i] = new RN();
			testers[i].start();
		}
		for (int i = 0; i < testers.length; i++) {
			testers[i].join();
		}
	}
}

class RN extends Thread {

	@Override
	public void run() {
		try {
			Class<DemoService> clz = DemoService.class;
			String name = "memory_setString";
			NettyTransceiver tran = new NettyTransceiver(new InetSocketAddress("localhost", 9999));
			Object param = new Object[] { "aaa", SmartNettyTransceiverTest2.data, 50000 };
			for (int i = 0; i < SmartNettyTransceiverTest2.loop; i++) {
				List<ByteBuffer> data1 = PackageTester.mock(clz, name, param);
				tran.transceive(data1);
			}
			tran.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}