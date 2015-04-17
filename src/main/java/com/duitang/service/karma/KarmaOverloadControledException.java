package com.duitang.service.karma;

public class KarmaOverloadControledException extends KarmaOverloadException {
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
