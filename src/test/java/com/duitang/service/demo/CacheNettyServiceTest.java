package com.duitang.service.demo;

import java.io.IOException;
import java.net.InetSocketAddress;

import junit.framework.Assert;

import org.apache.avro.ipc.NettyServer;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.base.ClientFactory;
import com.duitang.service.base.MetricCenter;
import com.duitang.service.base.MetricalReflectRequestor;
import com.duitang.service.base.MetricalReflectResponder;
import com.duitang.service.base.ServerBootstrap;
import com.duitang.service.mina.MinaTransceiver;

public class CacheNettyServiceTest {

	ServerBootstrap boot = null;
	ClientFactory<DemoService> fac = null;
	ServerBootstrap bootHttp = null;
	ClientFactory<DemoService> facHttp = null;

	@Before
	public void setUp() {
		LogManager.getLogger("org.apache.avro.ipc").setLevel(Level.ALL);
		MemoryCacheService impl = new MemoryCacheService(true);
		boot = new ServerBootstrap();
		boot.addService(DemoService.class, impl);
		try {
			boot.startUp(9090, "netty");
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		bootHttp = new ServerBootstrap();
		bootHttp.addService(DemoService.class, impl);
		try {
			bootHttp.startUp(9091, "http");
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl("netty://127.0.0.1:9090");

		facHttp = ClientFactory.createFactory(DemoService.class);
		facHttp.setUrl("http://127.0.0.1:9091");
	}

	@After
	public void destroy() {
		boot.shutdown();
		bootHttp.shutdown();
	}

	// @Test
	public void testSleep() {
		try {
			Thread.sleep(20000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testB() throws Exception {
		MemoryCacheService impl = new MemoryCacheService();
		MetricalReflectResponder responder = new MetricalReflectResponder(DemoService.class, impl);
		responder.setClientid("unittesting");
		NettyServer server = new NettyServer(responder, new InetSocketAddress(9099));
		server.start();

		// Transceiver tr = new NettyTransceiver(new
		// InetSocketAddress("localhost", 9099));
		MinaTransceiver tr = new MinaTransceiver("localhost:9090", 500);
		DemoService cli = MetricalReflectRequestor.getClient(DemoService.class, tr);
		try {
			String key = "aaaa";
			String value = "bbbb";
			System.out.println(cli.memory_setString(key, value, 1111));
			CharSequence sss = (CharSequence) cli.memory_getString(key);
			Assert.assertEquals(value, String.valueOf(sss));
			System.out.println(sss);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			// fac.release(cli);
		}
	}

	// @Test
	public void testBoot() {
		DemoService cli = fac.create();
		try {
			String key = "aaaa";
			String value = "bbbb";
			System.out.println(cli.memory_setString(key, value, 1111));
			CharSequence sss = (CharSequence) cli.memory_getString(key);
			Assert.assertEquals(value, String.valueOf(sss));
			System.out.println(sss);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			fac.release(cli);
		}
		cli = fac.create();
		try {
			String key = "aaaa";
			String value = "bbbb";
			System.out.println(cli.memory_setString(key, value, 1111));
			CharSequence sss = (CharSequence) cli.memory_getString(key);
			System.out.println(sss);
			Assert.assertEquals(value, String.valueOf(sss));
			System.out.println(sss);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			fac.release(cli);
		}

		long stime = 10L;
		// long stime = 10000000L;
		for (long i = 1000; i < stime; i += 1000) {
			try {
				// System.out.println(fac.isValid((Client) cli));
				Thread.sleep(1000);
				if (i > 5000) {
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// @Test
	public void testMetric() throws Exception {
		MetricCenter.enableConsoleReporter(1);
		DemoService cli = fac.create();
		for (int i = 0; i < 10; i++) {
			System.out.println("----->" + cli.trace_msg("wait_100", 100));
		}
		boot.shutdown();
		for (int i = 0; i < 5; i++) {
			try {
				System.out.println("----->" + cli.trace_msg("wait_600", 600));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		fac.release(cli);
		Thread.sleep(5000);
	}

	// @Test
	public void testCloseit() throws Exception {
		DemoService cli = fac.create();
		fac.release(cli);
	}

}
