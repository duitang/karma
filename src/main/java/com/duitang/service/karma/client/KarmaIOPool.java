package com.duitang.service.karma.client;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.KarmaNoMoreConnException;
import com.duitang.service.karma.base.LifeCycle;

import com.google.common.collect.Sets;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KarmaIOPool implements LifeCycle {

  final static protected Logger err = Logger.getLogger("error");
  protected ConcurrentHashMap<String, GenericObjectPool<KarmaIoSession>> ioPool = new ConcurrentHashMap<String, GenericObjectPool<KarmaIoSession>>();
  protected long timeout;
  protected volatile boolean closed = false;

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public void resetPool() {
    Set<Entry<String, GenericObjectPool<KarmaIoSession>>> set = Sets.newHashSet(ioPool.entrySet());
    ioPool.clear();//clear first
    for (Map.Entry<String, GenericObjectPool<KarmaIoSession>> e : set) {
      GenericObjectPool<KarmaIoSession> pool = e.getValue();
      if (pool != null) {
        pool.close();
      }
    }
  }

  public KarmaIoSession getIOSession(String url) throws KarmaException {
    if (closed) {
      throw new KarmaException("closed pool: " + url);
    }
    GenericObjectPool<KarmaIoSession> pool = ioPool.get(url);
    if (pool == null) {
      pool = forceCreatePool(url);
      ioPool.putIfAbsent(url, pool);
      pool = ioPool.get(url);
    }
    try {
      return pool.borrowObject(timeout);
    } catch (Exception e) {
      throw new KarmaNoMoreConnException(e.getMessage());
    }
  }

  public void releaseIOSession(KarmaIoSession session) {
    if (session == null) {
      return;
    }
    GenericObjectPool<KarmaIoSession> pool = ioPool.get(session.url);
    if (session.isAlive() && pool != null) {
      pool.returnObject(session);
    } else {
      if (pool == null) {
        try {
          session.close();
        } catch (IOException e) {
        }
        return;
      }
      try {
        pool.invalidateObject(session);
      } catch (Exception e) {
      }
    }
  }

  protected GenericObjectPool<KarmaIoSession> forceCreatePool(String url) {
    GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
    cfg.setMaxIdle(100);
    cfg.setMinIdle(0);
    cfg.setMaxTotal(300);
    cfg.setTestWhileIdle(false);
    cfg.setBlockWhenExhausted(true);
    cfg.setMaxWaitMillis(timeout);
    cfg.setMinEvictableIdleTimeMillis(120000);
    // cfg.setTestOnReturn(true); // may release it if idle
    cfg.setTestOnBorrow(true); // may release it if idle
    return new GenericObjectPool(new ReflectServiceFactory(url), cfg);
  }

  class ReflectServiceFactory implements PooledObjectFactory<KarmaIoSession> {

    protected String url;

    public ReflectServiceFactory(String url) {
      this.url = url;
    }

    @Override
    public PooledObject<KarmaIoSession> makeObject() throws Exception {
      try {
        KarmaIoSession session = new KarmaIoSession(url, timeout);
        // ret.init();
        return new DefaultPooledObject<KarmaIoSession>(session);
      } catch (Exception e) {
        err.error("create for service: " + url, e);
        throw e;
      }
    }

    @Override
    public void destroyObject(PooledObject<KarmaIoSession> p) throws Exception {
      KarmaIoSession obj = p.getObject();
      // System.out.println("destroy ..... " + obj);
      obj.close();
    }

    @Override
    public boolean validateObject(PooledObject<KarmaIoSession> p) {
      KarmaIoSession obj = p.getObject();
      // System.out.println("checking ..... " + ((Validation)
      // obj).isValid() + " ---> " + obj);
      return obj.isAlive();
    }

    @Override
    public void activateObject(PooledObject<KarmaIoSession> p) throws Exception {
      // ignore
      // System.out.println("...............active " + p.getObject());
      KarmaIoSession obj = p.getObject();
      obj.init();
    }

    @Override
    public void passivateObject(PooledObject<KarmaIoSession> p) throws Exception {
      // ignore
    }

  }

  public void close() {
    // may be race condition, but currently ok
    closed = true;
    if (ioPool == null) {
      return;
    }
    for (Entry<String, GenericObjectPool<KarmaIoSession>> en : ioPool.entrySet()) {
      err.info("closing ioPool ... " + en.getKey());
      en.getValue().close();
    }
  }

  @Override
  public void init() throws Exception {
    // ignore
  }

  @Override
  public boolean isAlive() {
    return !closed;
  }

}
