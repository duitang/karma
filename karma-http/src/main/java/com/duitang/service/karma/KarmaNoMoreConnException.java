package com.duitang.service.karma;

/**
 * @author kevx
 * @since 7:35:36 PM May 21, 2015
 */
public class KarmaNoMoreConnException extends KarmaOverloadException {

  private static final long serialVersionUID = -3338950361429293389L;

  public KarmaNoMoreConnException(String message) {
    super(message);
  }
}
