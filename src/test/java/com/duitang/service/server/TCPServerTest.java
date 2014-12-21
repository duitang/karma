package com.duitang.service.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.junit.Test;

import com.duitang.service.KarmaException;
import com.duitang.service.demo.DemoService;
import com.duitang.service.demo.MemoryCacheService;
import com.duitang.service.handler.ReflectRPCHandler;
import com.duitang.service.router.JavaRouter;

public class TCPServerTest {

	// @Test
	public void test0() throws IOException, InterruptedException {
		NioSocketAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(new LogOnly());
		acceptor.bind(new InetSocketAddress(9999));
		Thread.sleep(10000000);
	}

	@Test
	public void test1() throws KarmaException, InterruptedException {
		ServiceConfig conf = new ServiceConfig();
		MemoryCacheService mms = new MemoryCacheService();
		mms.memory_setString("aaaa", "bbbb", 5000);
		System.out.println("aaaa ---> " + mms.memory_getString("aaaa"));

		conf.addService(DemoService.class, mms);

		ReflectRPCHandler rpc = new ReflectRPCHandler();
		rpc.setConf(conf);
		rpc.init();

		JavaRouter rt = new JavaRouter();
		rt.setHandler(rpc);

		TCPServer tcps = new TCPServer();
		tcps.setRouter(rt);
		tcps.setPort(9999);
		tcps.start();

		Thread.sleep(10000000);
	}

}

class LogOnly extends IoHandlerAdapter {

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		System.out.println("rec1: " + new Date());
		Thread.sleep(5000);
		System.out.println("rec2: " + new Date());
		super.messageReceived(session, message);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		super.messageSent(session, message);
	}

}
