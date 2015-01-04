package com.duitang.service.karma.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaRuntimeException;
import com.duitang.service.karma.client.KarmaClient;

public abstract class ClientFactory<T> implements ServiceFactory<T> {

	final static protected Logger err = Logger.getLogger("error");

	protected String url;
	protected List<String> serviceURL;
	protected AtomicInteger hashid = new AtomicInteger(0);
	protected int timeout = 500;
	protected String clientid;
	protected int sz = 0;

	public ClientFactory() {
		this(null);
	}

	public ClientFactory(String clientid) {
		this.clientid = clientid;
		initClientName();
	}

	private void initClientName() {
		if (clientid == null) {
			clientid = MetricCenter.genClientIdFromCode();
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		String[] urlitems = url.split(";");
		this.serviceURL = new ArrayList<String>();
		for (String u : urlitems) {
			this.serviceURL.add(u);
		}
		this.sz = this.serviceURL.size();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public T create() { // for python performance issue no exception
		if (sz == 0) {
			new KarmaRuntimeException("no remote url find? please setUrl(String url)").printStackTrace();
			return null;
		}
		KarmaClient<T> ret;
		try {
			ret = KarmaClient.createKarmaClient(getServiceType(), serviceURL, clientid);
			return ret.getService();
		} catch (KarmaException e) {
		}
		return null;
	}

	public void release(T client) {
		// current for keep consistent
	}

	public static <T1> ClientFactory<T1> createFactory(final Class<T1> clz) {
		final String name = clz.getName();
		ClientFactory<T1> ret = new ClientFactory<T1>() {

			@Override
			public String getServiceName() {
				return name;
			}

			@Override
			public Class getServiceType() {
				return clz;
			}

		};
		MetricCenter.initMetric(clz, ret.clientid);
		return ret;
	}

	@SuppressWarnings("rawtypes")
	static public ClientFactory createServiceFactory(String serviceName) throws Exception {
		return ClientFactory.createFactory(Class.forName(serviceName));
	}

}
