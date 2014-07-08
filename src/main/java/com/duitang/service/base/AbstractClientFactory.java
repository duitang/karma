package com.duitang.service.base;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.cglib.proxy.Mixin;

import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.log4j.Logger;

public abstract class AbstractClientFactory<T> implements ServiceFactory<T> {

	protected Logger err = Logger.getLogger("error");

	// protected String protocol;
	protected String url;
	protected List<URL> serviceURL;
	protected AtomicInteger hashid = new AtomicInteger(0);
	protected int sz;
	protected int timeout = 500;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		String[] urlitems = url.split(";");
		this.serviceURL = new ArrayList<URL>();
		for (String u : urlitems) {
			try {
				this.serviceURL.add(new URL(u));
			} catch (MalformedURLException e) {
				throw new RuntimeException("set url: " + u, e);
			}
		}
		this.sz = this.serviceURL.size();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public T create() {
		T ret = null;
		try {
			HttpTransceiver client = new HttpTransceiver(serviceURL.get(hashid.incrementAndGet() % sz));
			client.setTimeout(timeout);
			ret = (T) SpecificRequestor.getClient(getServiceType(), client);
			ret = enhanceIt(ret, client);
		} catch (IOException e) {
			err.error("create for service: " + this.url, e);
		}
		return ret;
	}

	protected T enhanceIt(T client, Transceiver trans) {
		Mixin ret = Mixin.create(new Object[] { client, new WrappedTrans(trans) });
		return (T) ret;
	}

	public void release(T client) {
		if (client instanceof Closeable) {
			try {
				((Closeable) client).close();
			} catch (IOException e) {
				err.error(e);
			}
		}
	}

}

class WrappedTrans implements Closeable {

	protected Transceiver trans;

	public WrappedTrans(Transceiver trans) {
		this.trans = trans;
	}

	@Override
	public void close() throws IOException {
		if (this.trans != null) {
			this.trans.close();
		}
	}

}
