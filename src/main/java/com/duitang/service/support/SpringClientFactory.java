package com.duitang.service.support;

import com.duitang.service.base.ClientFactory;

public class SpringClientFactory<T> {

	protected Class<T> clientType;
	protected String url;

	public SpringClientFactory(Class<T> type) {
		this.clientType = type;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ClientFactory<T> createClient() {
		ClientFactory<T> ret = ClientFactory.createFactory(clientType);
		ret.setUrl(url);
		return ret;
	}

}
