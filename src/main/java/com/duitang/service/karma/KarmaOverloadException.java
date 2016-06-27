package com.duitang.service.karma;

/**
 * 服务端系统过载异常
 *
 * @author kevx
 * @since 3:14:35 PM Mar 16, 2015
 */
public class KarmaOverloadException extends KarmaRuntimeException {
  private static final long serialVersionUID = 2686990279083243711L;

  public KarmaOverloadException() {
  }

  public KarmaOverloadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public KarmaOverloadException(String message, Throwable cause) {
    super(message, cause);
  }

  public KarmaOverloadException(String message) {
    super(message);
  }

  public KarmaOverloadException(Throwable cause) {
    super(cause);
  }
}
