package com.duitang.service.karma.support;

import com.duitang.service.karma.base.ClientFactory;
import com.duitang.service.karma.client.ClusterZKRouter;

public class SpringClientFactory<T> {

	protected Class<T> clientType;
	protected String url;
	protected String group;
	protected int timeout;
	protected String zkURL;

	public SpringClientFactory(Class<T> type) {
		this.clientType = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setZkURL(String zkURL) {
		this.zkURL = zkURL;
	}

	public ClientFactory<T> createClient() {
		ClientFactory<T> ret = ClientFactory.createFactory(clientType);
		ret.setUrl(url);
		ret.setGroup(group);
		ret.setTimeout(timeout);
		if (zkURL != null) {
			ClusterZKRouter.enableZK(zkURL);
		}
		return ret;
	}

}
