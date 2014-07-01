package com.duitang.service.base;

import io.airlift.units.Duration;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.facebook.nifty.client.NiftyClient;

public abstract class AbstractClientFactory<T> implements ServiceFactory<T> {

	public static Duration DEFAULT_CONNECT_TIMEOUT = new Duration(2, TimeUnit.SECONDS);
	public static Duration DEFAULT_RECEIVE_TIMEOUT = new Duration(2, TimeUnit.SECONDS);
	public static Duration DEFAULT_READ_TIMEOUT = new Duration(2, TimeUnit.SECONDS);
	public static Duration DEFAULT_SEND_TIMEOUT = new Duration(2, TimeUnit.SECONDS);

	public static final int DEFAULT_MAX_FRAME_SIZE = 16777216;

	protected Logger err = Logger.getLogger("error");

	protected String url;
	protected String host;
	protected int port;

	public Duration INST_CONNECT_TIMEOUT = DEFAULT_CONNECT_TIMEOUT;
	public Duration INST_RECEIVE_TIMEOUT = DEFAULT_RECEIVE_TIMEOUT;
	public Duration INST_READ_TIMEOUT = DEFAULT_READ_TIMEOUT;
	public Duration INST_SEND_TIMEOUT = DEFAULT_SEND_TIMEOUT;
	public int INST_MAX_FRAME_SIZE = DEFAULT_MAX_FRAME_SIZE;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		this.host = url.split(":")[0];
		this.port = Integer.valueOf(url.split(":")[1]);
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

	@SuppressWarnings("resource")
	@Override
	public T create() {
		NiftyClient cli = null;
		TBinaryProtocol tp = null;
		try {
			cli = new NiftyClient();// will close at release
			InetSocketAddress address = new InetSocketAddress(host, port);
			// FIXME: notice performance issue here: poor sync connect
			TTransport transport = cli.connectSync(address, INST_CONNECT_TIMEOUT, INST_RECEIVE_TIMEOUT,
			        INST_SEND_TIMEOUT, INST_MAX_FRAME_SIZE);
			tp = new TBinaryProtocol(transport);
			return doCreate(tp, tp);
		} catch (Exception e) {
			err.error("create:", e);
			if (cli != null) {
				cli.close();
			}
			if (tp != null) {
				tp.getTransport().close();
			}
		}
		return null;
	}

	protected void doReleaseProtocol(TProtocol inprot, TProtocol outprot) {
		inprot.getTransport().close();
		outprot.getTransport().close();
	}

	protected abstract T doCreate(TProtocol inprot, TProtocol outprot);

}
