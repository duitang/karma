package com.duitang.service.karma.handler;

import com.duitang.service.karma.invoker.Invoker;

public class RPCContext {

	public String name;
	public String method;
	public Object[] params;
	public Invoker invoker;
	public Object ret;
	public Throwable ex;

}
