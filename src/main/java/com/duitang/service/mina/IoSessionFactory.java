package com.duitang.service.mina;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;

public class IoSessionFactory extends BasePooledObjectFactory<IoSession> {

	protected ConnectFuture cf;

	public IoSessionFactory(ConnectFuture cf) {
		this.cf = cf;
	}

	@Override
	public IoSession create() throws Exception {
		return cf.getSession();
	}

	@Override
	public PooledObject<IoSession> wrap(IoSession obj) {
		return new DefaultPooledObject<IoSession>(obj);
	}

	@Override
	public void destroyObject(PooledObject<IoSession> p) throws Exception {
		super.destroyObject(p);
		IoSession session = p.getObject();
		if (session != null) {
			session.close(false);
		}
	}

	@Override
	public boolean validateObject(PooledObject<IoSession> p) {
		boolean ret = false;
		IoSession session = p.getObject();
		if (session != null) {
			ret = session.isConnected();
		}
		return ret;
	}

}
