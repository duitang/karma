package com.duitang.service.karma.server;

import java.util.Date;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.router.Router;

public interface RPCService {

	public void start() throws KarmaException;

	public void stop();

	public void setRouter(Router router);

	public void setGroup(String grp);

	public Date getUptime();

	public String getGroup();

	public String getServiceURL();

	public String getServiceProtocol();

	public boolean online();

}
