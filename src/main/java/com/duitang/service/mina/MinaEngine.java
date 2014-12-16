package com.duitang.service.mina;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.filter.codec.ProtocolCodecFilter;

public class MinaEngine {

	static final protected List<MinaEpoll> epoll = new ArrayList<MinaEpoll>();
	static final protected int epoll_size = 4;
	// round robin
	static final protected AtomicInteger rr = new AtomicInteger(0);

	static {
		for (int i = 0; i < epoll_size; i++) {
			MinaEpoll m = new MinaEpoll();
			m.epoll.getSessionConfig().setTcpNoDelay(true);
			m.epoll.getSessionConfig().setKeepAlive(true);
			m.epoll.getFilterChain().addLast("codec", new ProtocolCodecFilter(new AvroCodecFactory()));
			m.epoll.setHandler(new MinaRPCHandler(m));
			epoll.add(m);
		}
	}

	static public MinaEpoll getEngine() throws Exception {
		int iid = MinaEngine.rr.getAndIncrement();
		iid = Math.abs(iid) % MinaEngine.epoll_size;
		return MinaEngine.epoll.get(iid);
	}

}
