package com.duitang.service.karma.client;

import com.duitang.service.karma.base.LifeCycle;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.server.KarmaHandlerInitializer;
import com.duitang.service.karma.transport.JavaClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * support logic ping but will be discard if too many ping happened
 *
 * @author laurence
 */
public class KarmaIoSession implements LifeCycle {

  static final protected long default_timeout = 500; // 0.5s
  static final protected int ERROR_WATER_MARK = 2;

  static final EventLoopGroup worker = new NioEventLoopGroup();
  static final KarmaHandlerInitializer starter = new KarmaHandlerInitializer(new JavaClientHandler());

  protected String url;
  protected long timeout = default_timeout;

  protected Bootstrap conn;
  protected ChannelFuture cf;
  protected Channel session;
  protected AtomicLong uuid = new AtomicLong(0);

  protected volatile int errorCount = 0;

  // not thread-safe
  protected boolean initialed = false;

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public int reportError() {
    return ++this.errorCount;
  }

  public void clearError() {
    this.errorCount = 0;
  }

  public void hitError() {
    this.errorCount = ERROR_WATER_MARK + 1;
  }

  public KarmaIoSession(String hostAndPort, long timeout) {
    this.url = hostAndPort;
    this.timeout = timeout;
    this.conn = new Bootstrap();
    this.conn.group(worker);
    this.conn.option(ChannelOption.TCP_NODELAY, true);
    this.conn.channel(NioSocketChannel.class).handler(new KarmaHandlerInitializer(new JavaClientHandler()));
    String[] uu = url.split(":");
    String host = uu[0];
    int port = Integer.valueOf(uu[1]).intValue();
    this.cf = this.conn.connect(new InetSocketAddress(host, port));
  }

  public void init() throws IOException {
    if (initialed) {
      return;
    }
    try {
      // ensure connect stable, should > 1s
      // so connect is very heavy action
      long t = timeout >= 2000 ? timeout : 2000;
      this.cf.await(t);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (!this.cf.isSuccess()) {
      this.close();
      throw new IOException("create connection to " + url + " failed!");
    }

    this.session = cf.channel();
    this.initialed = true;
  }

  public boolean isConnected() {
    return session.isActive();
  }

  public void write(BinaryPacketData data) {
    this.session.writeAndFlush(data.getBytes());
  }

  public boolean ping() {
    if (!this.session.isActive()) {
      hitError();
      return false;
    }
    // using 25% timeout for reduce cost
    KarmaRemoteLatch latch = new KarmaRemoteLatch(timeout / 4);
    latch.uuid = uuid.incrementAndGet();
    this.setAttribute(latch);
    this.session.writeAndFlush(BinaryPacketHelper.karmaPingBytes(1.0f, latch.uuid));
    try {
      latch.getResult();
      clearError();
      return true;
    } catch (Throwable e) {
      hitError();
      // direct out
    }
    return false;
  }

  public boolean reachable() {
    String[] ss = StringUtils.split(this.url, ":");
    if (ss == null || ss.length != 2) return false;
    try {
      InetAddress ia = InetAddress.getByName(ss[0]);
      return ia.isReachable(10);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public AtomicLong getUuid() {
    return uuid;
  }

  public void setAttribute(KarmaRemoteLatch latch) {
    Attribute<KarmaRemoteLatch> attr = this.session.attr(KarmaRemoteLatch.LATCH_KEY);
    attr.set(latch);
  }

  @Override
  public void close() throws IOException {
    if (session != null) {
      try {
        session.close().sync();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if (cf != null) {
      cf.cancel(true);
    }
  }

  @Override
  public boolean isAlive() {
    return errorCount < ERROR_WATER_MARK && this.isConnected();
  }

  @Override
  public String toString() {
    return url + ", timeout=" + timeout + ", errorCount=" + errorCount;
  }

}
