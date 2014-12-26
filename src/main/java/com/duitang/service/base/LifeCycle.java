package com.duitang.service.base;

import java.io.Closeable;

public interface LifeCycle extends Closeable {

	void init() throws Exception;

	boolean isAlive();

}
