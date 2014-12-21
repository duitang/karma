package com.duitang.service.server;

import com.duitang.service.KarmaException;
import com.duitang.service.router.Router;

public interface RPCService {

	public void start() throws KarmaException;

	public void stop();

	public void setRouter(Router router);

}
