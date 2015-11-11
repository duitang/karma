package com.duitang.service.karma.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务自动发现机制
 *
 * 每台机器启动后均将自身注册到zk中； 当机器失联后zk会自动删除相应节点
 *
 * 对于服务提供者还会将ServiceExporter暴露的接口写入ZK
 *
 * @author kevx
 * @since 5:57:54 PM Jan 13, 2015
 */
public class NodeRegister {

  private final static Logger log = Logger.getLogger(NodeRegister.class);
  private final static ObjectMapper mapper = new ObjectMapper();

  private final Map<String, String> extraData = Maps.newHashMap();

  private String appName;
  private boolean online = true;
  private ServicesExporter servicesExporter;

  private String getHost() throws UnknownHostException {
    InetAddress ia = InetAddress.getLocalHost();
    return ia.getHostAddress();
  }

  public void init() {
    try {
      final String host = getHost();
      final String data = makeData();
      final ACL acl = new ACL(Perms.ALL, Ids.ANYONE_ID_UNSAFE);
      ZkHolder.addCallback(new Runnable() {
        @Override
        public void run() {
          if (!isOnline()) {
            return;
          }

          final ZooKeeper zk = ZkHolder.get();
          try {
            String appPath = "/app/" + appName;
            if (zk.exists(appPath, false) == null) {
              zk.create(
                  appPath,
                  null,
                  Lists.newArrayList(acl),
                  CreateMode.PERSISTENT
              );
            }

            String nodePath = appPath + '/' + host;
            if (zk.exists(nodePath, false) == null) {
              zk.create(
                  nodePath,
                  data.getBytes(),
                  Lists.newArrayList(acl),
                  CreateMode.EPHEMERAL
              );
            }
          } catch (KeeperException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, false);
    } catch (Exception e) {
      log.error("NodeRegister_failed:", e);
    }
  }

  public void unregister() {
    try {
      String appPath = "/app/" + appName;
      String nodePath = appPath + '/' + getHost();
      final ZooKeeper zk = ZkHolder.get();
      Stat status = zk.exists(nodePath, false);
      if (status != null) {
        zk.delete(nodePath, status.getVersion());
      }
    } catch (Exception e) {
      log.error("NodeUnregister_failed:", e);
    }
  }

  public String makeData() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    String now = sdf.format(new Date());
    Map<String, String> m = Maps.newHashMap();
    m.putAll(extraData);
    m.put("rpc_gmt_create", now);
    if (servicesExporter != null) {
      //this is a service provider
      List<String> all = servicesExporter.getExportedInterfaces();
      if (all.size() > 0) {
        m.put("rpc_interfaces", Joiner.on(';').join(all));
        m.put("rpc_port", String.valueOf(servicesExporter.getPort()));
      }
    }
    return mapper.writeValueAsString(m);
  }

  public boolean isDev() {
    try {
      InetAddress ia = InetAddress.getLocalHost();
      if (IpRanges.isProduction(ia.getHostAddress())) {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  public synchronized boolean isOnline() {
    return online;
  }

  public synchronized void setOffline() {
    online = false;
    unregister();
  }

  public synchronized void setOnline() {
    online = true;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public void setServicesExporter(ServicesExporter servicesExporter) {
    this.servicesExporter = servicesExporter;
  }

  public void setExtraData(Map<String, String> extraData) {
    addExtraData(extraData);
  }

  public void addExtraData(Map<String, String> ext) {
    if (ext != null) extraData.putAll(ext);
  }
}
