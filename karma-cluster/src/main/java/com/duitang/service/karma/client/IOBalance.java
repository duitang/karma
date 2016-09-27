package com.duitang.service.karma.client;

import java.util.Map;

public interface IOBalance {

  /**
   * fetch next token from router
   *
   * @param token current used token
   * @return next token
   */
  public String next(String token);

  public void updateLoad(Map<String, Integer> load);

  public void fail(String token);
}
