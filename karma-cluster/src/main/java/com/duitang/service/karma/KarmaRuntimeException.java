package com.duitang.service.karma;

public class KarmaRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public KarmaRuntimeException() {
    super();
  }

  public KarmaRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public KarmaRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public KarmaRuntimeException(String message) {
    super(message);
  }

  public KarmaRuntimeException(Throwable cause) {
    super(cause);
  }

}
