package com.duitang.service.base;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.log4j.Logger;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;

public abstract class AbstractClientFactory<T> implements ServiceFactory<T> {

	protected Logger err = Logger.getLogger("error");

	protected String url;
	protected List<URL> serviceURL;
	protected Meter qps;
	protected Histogram dur;
	protected Meter qps_f;
	protected Histogram dur_f;
	protected AtomicInteger hashid = new AtomicInteger(0);
	protected int sz;
	protected int timeout = 500;
	protected String clientid;
	protected TraceableObject<T> tracer;

	public AbstractClientFactory() {
		this(null);
	}

	public AbstractClientFactory(String clientid) {
		this.clientid = clientid;
		initClientName();
		init();
	}

	protected void init() {
		MetricCenter.initMetric(getServiceType(), clientid);
		tracer = new TraceableObject<T>();
	}

	protected void initClientName() {
		if (clientid == null) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			StackTraceElement e = stacktrace[5];
			if (e.getMethodName() != null) {
				clientid = MetricCenter.getHostname() + "|" + e.getFileName() + "@" + e.getLineNumber() + ":"
				        + e.getMethodName();
			}
		}
		if (clientid == null) {
			clientid = "";
		}
	}

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
		MetricableHttpTransceiver.setTimeout(timeout);
	}

	@Override
	public T create() {
		T ret = null;
		try {
			MetricableHttpTransceiver client = new MetricableHttpTransceiver(this.clientid, serviceURL.get(hashid
			        .incrementAndGet() % sz));
			ret = (T) SpecificRequestor.getClient(getServiceType(), client);
			ret = tracer.createTraceableInstance(ret, getServiceType(), clientid);
		} catch (IOException e) {
			err.error("create for service: " + this.url, e);
		}
		return ret;
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
