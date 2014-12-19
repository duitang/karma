package com.duitang.service;

public class KarmaException extends Exception {

	private static final long serialVersionUID = 1L;

	// FIXME: add global audit code here, e.g. AtomicInteger for counter

	public KarmaException(String message) {
		super(message);
	}

	public KarmaException(String message, Throwable cause) {
		super(message, cause);
	}

	public KarmaException(Throwable cause) {
		super(cause);
	}

	public KarmaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
