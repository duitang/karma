package com.duitang.service.handler;

import com.duitang.service.KarmaException;

public interface RPCHandler {

	/**
	 * lookup for rpc invoker
	 * 
	 * @param name
	 * @param method
	 * @param params
	 * @return
	 * @throws KarmaException
	 */
	public void lookUp(RPCContext ctx) throws KarmaException;

	/**
	 * invoke an rpc
	 * 
	 * @param proxy
	 * @param name
	 * @param method
	 * @param params
	 * @return
	 * @throws KarmaException
	 */
	public void invoke(RPCContext ctx) throws KarmaException;

}
