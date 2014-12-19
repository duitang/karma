package com.duitang.service.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.junit.Test;

public class TestMinaConnector {

	// @Test
	public void test0() {
		MinaEpoll m = new MinaEpoll();
		m.epoll.getSessionConfig().setTcpNoDelay(true);
		m.epoll.getSessionConfig().setKeepAlive(true);
		m.epoll.getFilterChain().addLast("codec", new ProtocolCodecFilter(new AvroCodecFactory()));
		m.epoll.setHandler(new MinaRPCHandler(m));
		for (int i = 0; i < 100000; i++) {
			ConnectFuture ret = m.epoll.connect(new InetSocketAddress("s3", 9999));
			try {
				ret.await(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println(" connected: " + ret.isConnected());
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void test1() {
		while (true) {
			MinaTransceiver trans = null;
			try {
				trans = new MinaTransceiver("s3:9999", 500);
				trans.init();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (trans != null) {
						trans.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}
}
