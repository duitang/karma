package com.duitang.service.server;

import com.duitang.service.KarmaException;

public interface RPCService {

	public void start() throws KarmaException;

	public void stop();

}
