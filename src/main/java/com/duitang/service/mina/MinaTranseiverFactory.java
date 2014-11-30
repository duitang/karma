package com.duitang.service.mina;

import java.net.InetSocketAddress;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class MinaTranseiverFactory extends BasePooledObjectFactory<MinaTransceiver> {

	protected String url;
	protected InetSocketAddress addr;

	public MinaTranseiverFactory(String url) {
		String[] u = url.split(":");
		this.addr = new InetSocketAddress(u[0], Integer.valueOf(u[1]));
		this.url = url;
	}

	@Override
	public MinaTransceiver create() throws Exception {
		return new MinaTransceiver(url, addr);
	}

	@Override
	public PooledObject<MinaTransceiver> wrap(MinaTransceiver obj) {
		return new DefaultPooledObject<MinaTransceiver>(obj);
	}

	@Override
	public void destroyObject(PooledObject<MinaTransceiver> p) throws Exception {
		p.getObject().release();
	}

	@Override
	public boolean validateObject(PooledObject<MinaTransceiver> p) {
		return p.getObject().isAlive();
	}

}
