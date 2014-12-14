package com.duitang.service.mina;

import org.apache.avro.Protocol;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;

public class MinaSocket {

	final public MinaEpoll epoll;
	public ConnectFuture connection;
	public IoSession session;
	public Protocol remote;

	public MinaSocket(MinaEpoll epoll) {
		this.epoll = epoll;
	}

}
