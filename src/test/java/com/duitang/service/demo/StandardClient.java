package com.duitang.service.demo;

import com.duitang.service.karma.support.RpcClientConfig;
import com.duitang.service.karma.support.ServicesHolder;

public class StandardClient {

	public static void main(
	        String[] args) {
		RpcClientConfig cfg = new RpcClientConfig();
		cfg.setAppName("mandala");
		cfg.setGroup("japa");
		cfg.setTimeout(5000);
		cfg.setConnString("s15:3881");
		cfg.setUsingStaticRpcEndpoint(false);
		cfg.setStaticRpcEndpoint("s29:11001");
		
		ServicesHolder service = new ServicesHolder();
		service.setInterfaceName("com.duitang.service.biz.ICatService");
		service.setRpcClientConfig(cfg);
		
		service.create();
	}

}
