package com.duitang.service.cache;

import org.apache.thrift.protocol.TProtocol;

import com.duitang.service.base.AbstractClientFactory;

public class CacheClientFactory extends AbstractClientFactory<Cache.Client> {

	final static String servicename = "CacheService";

	@Override
	public void release(Cache.Client cli) {
		doReleaseProtocol(cli.getInputProtocol(), cli.getOutputProtocol());
	}

	@Override
	protected Cache.Client doCreate(TProtocol inprot, TProtocol outprot) {
		return new Cache.Client(inprot, outprot);
	}

	@Override
	public String getServiceName() {
		return servicename;
	}

}
