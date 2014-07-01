package com.duitang.service.demo;

import org.apache.thrift.protocol.TProtocol;

import com.duitang.service.base.AbstractClientFactory;

public class MemoryCacheClientFactory extends AbstractClientFactory<MemoryCache.Client> {

	final static String servicename = MemoryCacheService.class.getName();
	
	@Override
	public void release(MemoryCache.Client cli) {
		doReleaseProtocol(cli.getInputProtocol(), cli.getOutputProtocol());
	}

	@Override
	protected MemoryCache.Client doCreate(TProtocol inprot, TProtocol outprot) {
		return new MemoryCache.Client(inprot, outprot);
	}

	@Override
	public String getServiceName() {
		return servicename;
	}

}
