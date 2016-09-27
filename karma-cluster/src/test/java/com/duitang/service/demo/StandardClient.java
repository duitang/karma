package com.duitang.service.demo;

import com.duitang.service.karma.support.RpcClientConfig;
import com.duitang.service.karma.support.ServicesHolder;

public class StandardClient {

  public static void main(
      String[] args) {


    RpcClientConfig cfg = new RpcClientConfig();
    cfg.setAppName("mandala");
    cfg.setGroup("default_mandala");
    cfg.setTimeout(5000);
    cfg.setConnString("std-1.zk.infra.duitang.net:3881,std-2.zk.infra.duitang.net:3881,std-3.zk.infra.duitang.net:3881");
    cfg.setUsingStaticRpcEndpoint(false);
    cfg.setStaticRpcEndpoint("");
    cfg.init();

    ServicesHolder service = new ServicesHolder();
    service.setInterfaceName("com.duitang.service.biz.ICatService");
    service.setRpcClientConfig(cfg);
    service.init();

    Object cli = service.create();
    System.out.println(cli);
  }

}
