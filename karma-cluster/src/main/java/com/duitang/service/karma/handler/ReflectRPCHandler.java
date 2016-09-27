package com.duitang.service.karma.handler;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.base.ClientId;
import com.duitang.service.karma.invoker.ReflectInvoker;
import com.duitang.service.karma.server.ServiceConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ReflectRPCHandler implements RPCHandler {

  protected ServiceConfig conf;

  /**
   * native
   */
  protected Map<String, ReflectInvoker> services;

  public ServiceConfig getConf() {
    return conf;
  }

  public void setConf(ServiceConfig conf) {
    this.conf = conf;
  }

  public void init() throws KarmaException {
    services = new HashMap<String, ReflectInvoker>();
    for (Entry<Class, Object> en : conf.getServices().entrySet()) {
      String name = en.getKey().getName();
      //			String clientid = en.getKey().getName() + "-->" + en.getValue().getClass().getName() + "@" + MetricCenter.getHostname();
      ClientId clientId = new ClientId(en.getKey().getName(), false);
      ReflectInvoker impl = new ReflectInvoker(clientId, en.getKey(), en.getValue());
      services.put(name, impl);
    }
  }

  @Override
  public void lookUp(RPCContext ctx) throws KarmaException {
    ReflectInvoker ret = services.get(ctx.name);
    if (ret == null) {
      throw new KarmaException("domain[" + ctx.name + "] not found");
    }
    ctx.invoker = ret;
  }

  @Override
  public void invoke(RPCContext ctx) throws KarmaException {
    ctx.ret = ctx.invoker.invoke(ctx.method, ctx.params);
  }

}
