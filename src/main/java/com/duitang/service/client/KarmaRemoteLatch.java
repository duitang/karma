package com.duitang.service.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.duitang.service.KarmaRuntimeException;

public class KarmaRemoteLatch {

	final static public String LATCH_NAME = "KarmaRemoteLatch";
	protected CountDownLatch latch;
	protected Object result;
	protected Throwable ex;
	protected long timeout;
	protected boolean canThrowIt;

	public KarmaRemoteLatch() {
		this(500);
	}

	public KarmaRemoteLatch(long timeout) {
		this(timeout, false);
	}

	public KarmaRemoteLatch(long timeout, boolean canThrowIt) {
		this.timeout = timeout;
		this.latch = new CountDownLatch(1);
		this.canThrowIt = canThrowIt;
	}

	public void offerResult(Object val) {
		this.result = val;
		latch.countDown();
	}

	public void offerError(Throwable ex) {
		this.ex = ex;
		latch.countDown();
	}

	public Object getResult() throws Throwable {
		try {
			if (!latch.await(timeout, TimeUnit.MILLISECONDS) || this.ex != null) {
				throwIt(ex);
			}
		} catch (InterruptedException e) {
			throwIt(null);
		}
		return result;
	}

	protected void throwIt(Throwable t) throws KarmaRuntimeException {
		// if (!canThrowIt) {
		// return;
		// }
		KarmaRuntimeException ret = null;
		if (t == null) {
			ret = new KarmaRuntimeException("rpc call timeout = " + timeout + "ms");
		} else {
			if (!KarmaRuntimeException.class.isAssignableFrom(t.getClass())) {
				ret = new KarmaRuntimeException(t);
			} else {
				ret = (KarmaRuntimeException) t;
			}
		}
		throw ret;
	}

}
