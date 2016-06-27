package com.duitang.service.karma.support;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author kevx
 * @since 12:02:35 PM Jun 25, 2015
 */
public class NodeHolder {

  private final Map<String, Set<String>> data = Maps.newHashMap();

  public void init() {
    ZkHolder.addCallback(new Runnable() {
      @Override
      public void run() {
        ZooKeeper zk = ZkHolder.get();
        try {
          List<String> apps = zk.getChildren("/app", false);
          if (apps != null) {
            for (String app : apps) {
              List<String> nodes = zk.getChildren("/app/" + app, false);
              Set<String> nodeset = Sets.newHashSet();
              if (nodes != null) nodeset.addAll(nodes);
              data.put(app, nodeset);
            }
          }
        } catch (KeeperException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, true);
  }

  public Set<String> getNodes(String appName) {
    return data.get(appName);
  }

}
