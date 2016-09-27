package com.duitang.service.karma.server;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.router.Router;

public interface RPCService {

  public void start() throws KarmaException;

  public void stop();

  public void setRouter(Router router);

}
