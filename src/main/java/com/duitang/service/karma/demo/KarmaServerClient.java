package com.duitang.service.karma.demo;

import com.duitang.service.karma.support.NodeRegister;
import com.duitang.service.karma.support.RpcClientConfig;
import com.duitang.service.karma.support.ServicesExporter;
import com.duitang.service.karma.support.ServicesHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by alex on 6/1/16.
 */
public class KarmaServerClient {
  final static String[] PARAMETER_KEYS = {"server", "client", "port", "parallel", "delay"};
  final static String appName = "karmaTest";
  final static String appGroup = "karmaTest";

  static Map<String, String> argsToMap(String[] args) {
    Map<String, String> ret = new HashMap<String, String>();
    for (String item : args) {
      if (item.startsWith("--")) {
        String line = item.substring("--".length());
        String[] kv = line.split("=");
        if (kv.length == 2) {
          ret.put(kv[0], kv[1]);
        } else if (kv.length == 1) {
          ret.put(kv[0], "true");
        }
      }
    }
    return ret;
  }

  static void printUsage() {
    System.out.println("support paramter: ");
    for (String s : PARAMETER_KEYS) {
      System.out.println("    --" + s + "=");
    }
  }

  private static void runServer(final Map<String, String> param) {
    final ServicesExporter servicesExporter = new ServicesExporter();
    final int port = Integer.parseInt(param.get("port"));
    servicesExporter.setPort(port);
    servicesExporter.setMaxQueuingLatency(1000);
    IDemoService demoService = new MemoryCacheService();
    List<Object> services = new ArrayList<>();
    services.add(demoService);
    servicesExporter.setServices(services);
    servicesExporter.init();

    final NodeRegister nodeRegister = new NodeRegister();
    nodeRegister.setAppName(appName);
    nodeRegister.setServicesExporter(servicesExporter);
    nodeRegister.init();
    System.out.println("server running at port: " + port + " " + new Date());
    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        nodeRegister.setOffline();
        System.out.println("offline now! " + new Date());
        try {
          Thread.sleep(Integer.parseInt(param.get("delay")));
          System.out.println("exited now! " + new Date());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static void main(String[] args) {
    Map<String, String> param = argsToMap(args);
    if (param.containsKey("client")) {
      runClient(param);
    } else if (param.containsKey("server")) {
      runServer(param);
    } else {
      printUsage();
      System.exit(1);
    }
  }

  public static void runClient(Map<String, String> param) {
    RpcClientConfig cfg = new RpcClientConfig();
    cfg.setAppName(appName);
    cfg.setGroup(appGroup);
    cfg.setTimeout(1000);
    cfg.setConnString("10.1.4.11:3881");
    cfg.setUsingStaticRpcEndpoint(false);
    cfg.setStaticRpcEndpoint("");
    cfg.init();

    ServicesHolder service = new ServicesHolder();
    service.setInterfaceName("com.duitang.service.karma.demo.IDemoService");
    service.setRpcClientConfig(cfg);
    service.init();

    IDemoService cli = (IDemoService) service.create();
    bench(cli, Integer.parseInt(param.get("parallel")));
  }

  private static void bench(final IDemoService cli, int parallel) {
    final String key = "hello";
    final String val = "world";
    List<Thread> workers = new ArrayList<>();

    for (int i = 0; i < parallel; i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          while (true) {
            try {
              cli.memory_setString(key, val, 1000);
              String res = cli.memory_getString(key);
              if(new Random().nextInt(10) > 8) {
                System.out.println("sent " + val + " got " + res + " " + new Date());
              }
            } catch (Exception e) {
              System.out.println(e);
            }

            try {
              Thread.sleep(50);
            } catch (InterruptedException e) {
              break;
            }
          }
        }
      });
      t.start();
      workers.add(t);
    }
    for (Thread worker : workers) {
      try {
        worker.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }
}
