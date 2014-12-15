package com.duitang.service.mina;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.ipc.Callback;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class MinaEpoll {

	final public AtomicInteger uuid = new AtomicInteger(0);
	final public ConcurrentHashMap<Integer, Callback<List<ByteBuffer>>> callbacks = new ConcurrentHashMap<Integer, Callback<List<ByteBuffer>>>();
	final public NioSocketConnector epoll = new NioSocketConnector(2);

}
