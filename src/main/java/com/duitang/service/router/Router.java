package com.duitang.service.router;

import com.duitang.service.KarmaException;
import com.duitang.service.handler.RPCContext;
import com.duitang.service.handler.RPCHandler;

public interface Router<T> {

	public void route(RPCContext ctx, T ret) throws KarmaException;

	public void setHandler(RPCHandler handler);

}
