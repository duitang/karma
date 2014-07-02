package com.duitang.service.base;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.cglib.proxy.Mixin;

import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.log4j.Logger;

public abstract class AbstractClientFactory<T> implements ServiceFactory<T> {

	protected Logger err = Logger.getLogger("error");

	protected String protocol;
	protected String url;
	protected String host;
	protected int port;
	protected URL serviceURL;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		if (url.contains("://")) {
			String[] items = url.split("://");
			this.protocol = items[0];
			url = items[1];
		}
		this.host = url.split(":")[0];
		this.port = Integer.valueOf(url.split(":")[1]);
		try {
			this.serviceURL = new URL(this.url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("set url: " + url, e);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public T create() {
		T ret = null;
		try {
			HttpTransceiver client = new HttpTransceiver(serviceURL);
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
