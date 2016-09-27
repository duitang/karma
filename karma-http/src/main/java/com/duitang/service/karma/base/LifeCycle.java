package com.duitang.service.karma.base;

import java.io.Closeable;

public interface LifeCycle extends Closeable {

  void init() throws Exception;

  boolean isAlive();

}
