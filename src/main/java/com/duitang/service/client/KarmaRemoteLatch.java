package com.duitang.service.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.duitang.service.KarmaException;

public class KarmaRemoteLatch {

	final static public String LATCH_NAME = "KarmaRemoteLatch";
	protected CountDownLatch latch;
	protected Object result;
	protected Throwable ex;
	protected long timeout;

	public KarmaRemoteLatch() {
		this(500);
	}

	public KarmaRemoteLatch(long timeout) {
		this.timeout = timeout;
		this.latch = new CountDownLatch(1);
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
				if (ex != null) {
					throw ex;
				}
				throw new KarmaException("rpc call timeout = " + timeout + "ms");
			}
		} catch (InterruptedException e) {
			throw e;
		}
		return result;
	}

}
