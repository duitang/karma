package com.duitang.service.karma;

public class KarmaOverloadControledException extends KarmaOverloadException {
    private static final long serialVersionUID = -4730692929991321285L;

    public KarmaOverloadControledException() {
    }

    public KarmaOverloadControledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public KarmaOverloadControledException(String message, Throwable cause) {
        super(message, cause);
    }

    public KarmaOverloadControledException(String message) {
        super(message);
    }

    public KarmaOverloadControledException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
