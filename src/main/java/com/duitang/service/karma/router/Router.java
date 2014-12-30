package com.duitang.service.karma.router;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.handler.RPCHandler;

public interface Router<T> {

	public void route(RPCContext ctx, T ret) throws KarmaException;

	public void setHandler(RPCHandler handler);

}
