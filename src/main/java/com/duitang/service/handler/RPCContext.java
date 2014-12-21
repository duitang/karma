package com.duitang.service.handler;

import java.util.HashMap;
import java.util.Map;

import com.duitang.service.invoker.Invoker;

public class RPCContext {

	public String name;
	public String method;
	public Object[] params;
	public Invoker invoker;
	public Object ret;
	public Throwable ex;
	public Map attr = new HashMap();

}
