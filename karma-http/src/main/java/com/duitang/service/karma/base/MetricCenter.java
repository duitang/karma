package com.duitang.service.karma.base;

import com.duitang.service.karma.stats.CustomDataReporter;
import com.duitang.service.karma.stats.DWMetricReporter;
import com.duitang.service.karma.stats.InstanceTag;
import com.duitang.service.karma.stats.InstanceTagHolder;
import com.duitang.service.karma.stats.KafkaReporter;
import com.duitang.service.karma.stats.KarmaMetricHolder;
import com.duitang.service.karma.stats.Reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MetricCenter.record("com.duitang.example.service.SomeService.methodName", 20); // record in
 * nanos
 */
public class MetricCenter {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  final static String[] NOT_IN_PACKAGE_NAME = {"com.duitang.service.karma"};// "com.duitang.webx",

  private static ConcurrentHashMap<String, MetricUnit> metricUnits = new ConcurrentHashMap<>();

  /**
   * abbreviate the full class name
   *
   * @param fullClassName full class name, say com.duitang.infrastructure.utils.StringUtil
   * @return abbreviated name, say c.d.i.u.StringUtil
   */
  private static String abbreviate(String fullClassName) {
    String shortName = "";
    if (fullClassName == null || fullClassName.isEmpty())
      return shortName;

    char leadingChar = fullClassName.charAt(0);
    boolean newStart = false;
    int lastDotPosition = 0;
    int index = 0;
    for (char c : fullClassName.toCharArray()) {
      if (newStart) {
        shortName += leadingChar;
        shortName += '.';
        leadingChar = c;
        lastDotPosition = index;
      }
      newStart = c == '.';
      index++;
    }
    return shortName + fullClassName.substring(lastDotPosition);
  }

  public static String metricName(ClientId clientId, String method, boolean failure) {
    StringBuilder b = new StringBuilder()
        .append(abbreviate(clientId.getName()))
        .append('.')
        .append(method);

    b.append(clientId.isClient() ? ".CLIENT" : ".SERVER");

    if (failure) {
      b.append(".FAILURE");
    }
    return b.toString();
  }

  private static MetricUnit metricUnitFor(ClientId clientId, String method, boolean failure) {
    String name = metricName(clientId, method, failure);
    return metricUnitFor(name);
  }

  private static MetricUnit metricUnitFor(String name) {
    MetricUnit metricUnit = metricUnits.get(name);
    if (metricUnit == null) {
      synchronized (MetricCenter.class) {
        metricUnit = metricUnits.get(name);
        if (metricUnit == null) {
          metricUnit = new MetricUnit(name);
          metricUnits.put(name, metricUnit);
        }
      }
    }
    return metricUnit;
  }

  public static void record(ClientId clientId, String method, long elapse, boolean failure) {
    metricUnitFor(clientId, method, failure).record(elapse);
  }

  public static void record(String name, long elapse) {
    metricUnitFor(name).record(elapse);
  }

  /**
   * HOSTNAME or randomly generated string
   */
  static public String getHostname() {
    return InstanceTagHolder.getHostname();
  }

  static public void setAppName(String name) {
    if (name == null) {
      throw new NullPointerException("name==null");
    }
    InstanceTagHolder.setAppName(name);
    InstanceTagHolder.resetFinalTag();
  }

  static public void setHostname(String hostname) {
    if (hostname == null) {
      throw new NullPointerException("hostname==null");
    }
    InstanceTagHolder.setHostname(hostname);
    InstanceTagHolder.resetFinalTag();
  }

  static public InstanceTag getInstanceTag() {
    return InstanceTagHolder.INSTANCE_TAG;
  }

  public static String genClientIdFromCode() {
    StackTraceElement[] trac = Thread.currentThread().getStackTrace();
    String ret = "";
    String ss;
    boolean flag = false;
    for (int i = 3; i < trac.length; i++) {
      ret = trac[i].getClassName();
      ss = ret.toLowerCase();
      flag = false;
      for (String sss : NOT_IN_PACKAGE_NAME) {
        if (ret.startsWith(sss)) {
          flag = true;
          break;
        }
      }
      if (flag) {
        continue;
      }
      if (ss.contains("duitang")) {
        ret = trac[i].toString();
        break;
      }
    }
    return ret + "@" + getHostname();
  }

  public static List<Map> sample() {
    List<Map> samples = new ArrayList<>();
    for (Map.Entry<String, MetricUnit> entry : metricUnits.entrySet()) {
      samples.add(entry.getValue().sample());
    }
    return samples;
  }

  public static List<Map> getLatestMetric() {
    return KarmaMetricHolder.getMetricHolder().holding;
  }

  @Deprecated
  public static void enable() {
    KarmaMetricHolder.getReporterDaemon().start();
  }

  @Deprecated
  public static void enableKafkaReporter() {
    KarmaMetricHolder.getReporterDaemon().addReporter(new KafkaReporter());
  }

  @Deprecated
  public static void enableHolderReporter() {
    KarmaMetricHolder.getReporterDaemon().addReporter(
        KarmaMetricHolder.getMetricHolder().reporter());
  }

  @Deprecated
  public static void enableDWMetricReporter() {
    addCustomReporter(new DWMetricReporter());
  }

  public static void start(boolean isEnabled) {
    if (!isEnabled) {
      return;
    }
    KarmaMetricHolder.getReporterDaemon().start();
  }

  public static void startKafkaReporter(boolean isEnabled) {
    if (!isEnabled) {
      return;
    }
    KarmaMetricHolder.getReporterDaemon().addReporter(new KafkaReporter());
  }

  public static void startHolderReporter(boolean isEnabled) {
    if (!isEnabled) {
      return;
    }
    KarmaMetricHolder.getReporterDaemon().addReporter(
        KarmaMetricHolder.getMetricHolder().reporter());
  }

  public static void startDWMetricReporter(boolean isEnabled) {
    if (!isEnabled) {
      return;
    }
    addCustomReporter(new DWMetricReporter());
  }

  public static void setReportInterval(int second) {
    KarmaMetricHolder.getReporterDaemon().reportInterval(second);
  }

  public static void addReporter(Reporter r) {
    KarmaMetricHolder.getReporterDaemon().addReporter(r);
  }

  public static void addCustomReporter(CustomDataReporter r) {
    KarmaMetricHolder.getReporterDaemon().addReporter(r);
  }

}

