package com.duitang.service.karma.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.duitang.service.karma.KarmaRuntimeException;
import com.duitang.service.karma.KarmaTimeoutException;
import com.duitang.service.karma.trace.TraceCell;

import io.netty.util.AttributeKey;

public class KarmaRemoteLatch {

	final static public String LATCH_NAME = "KarmaRemoteLatch";
	final static public AttributeKey<KarmaRemoteLatch> LATCH_KEY = AttributeKey.valueOf(LATCH_NAME);

	final static String[][] types = new String[][] { { "cs", "cr" }, { "sr", "ss" } };

	protected CountDownLatch latch;
	protected Object result;
	protected Throwable ex;
	protected long timeout;
	protected boolean canThrowIt;
	protected long uuid;
	protected TraceCell tc;

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

	public void setTraceCell(TraceCell tc) {
		this.tc = tc;
	}

	public long getUuid() {
		return uuid;
	}

	public void setUuid(long uuid) {
		this.uuid = uuid;
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
			if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
				throw new KarmaTimeoutException();
			}
			if (this.ex != null)
				throw this.ex;
		} catch (InterruptedException e) {
			throw new KarmaRuntimeException("Interrupted");
		}
		return result;
	}

	/**
	 * Get the result without timeout.
	 * 
	 * @return result
	 */
	public Object readResult() {
		return result;
	}

	public void done() {
		tc.passivate(this.ex);
	}

}
