package com.duitang.service.karma;

public class KarmaOverloadControlledException extends KarmaOverloadException {
    private static final long serialVersionUID = -4730692929991321285L;

    public KarmaOverloadControlledException() {
    }

    public KarmaOverloadControlledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public KarmaOverloadControlledException(String message, Throwable cause) {
        super(message, cause);
    }

    public KarmaOverloadControlledException(String message) {
        super(message);
    }

    public KarmaOverloadControlledException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
