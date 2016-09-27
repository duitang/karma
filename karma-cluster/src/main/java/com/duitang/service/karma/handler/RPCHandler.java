package com.duitang.service.karma.handler;

import com.duitang.service.karma.KarmaException;

public interface RPCHandler {

  /**
   * lookup for rpc invoker
   */
  public void lookUp(RPCContext ctx) throws KarmaException;

  /**
   * invoke an rpc
   */
  public void invoke(RPCContext ctx) throws KarmaException;

}
