package com.duitang.service.karma.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.stats.*;

/**
 *  MetricCenter.record("com.duitang.example.service.SomeService.methodName", 20); // record in nanos
 */
public class MetricCenter {
	final static Logger logger = LoggerFactory.getLogger(MetricCenter.class);

	final static String[] NOT_IN_PACKAGE_NAME = { "com.duitang.service.karma" };// "com.duitang.webx",

    private static ConcurrentHashMap<String, MetricUnit> metricUnits = new ConcurrentHashMap<>();

    public static String metricName(ClientId clientId, String method, boolean failure) {
        StringBuilder b = new StringBuilder()
                .append(clientId.getName())
                .append('.')
                .append(method);

        b.append(clientId.isClient() ? ".CLIENT" : ".SERVER");

        if(failure) {
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
     *  HOSTNAME or randomly generated string
     */
	static public String getHostname() {
		return LocationHolder.getHostname();
	}

    static public void setAppName(String name) {
        if (name == null) {
            throw new NullPointerException("name==null");
        }
        LocationHolder.setAppName(name);
        LocationHolder.resetLocation();
    }

    static public void setHostname(String hostname) {
        if (hostname == null) {
            throw new NullPointerException("hostname==null");
        }
        LocationHolder.setHostname(hostname);
        LocationHolder.resetLocation();
    }

    static public LocationTag getLocation() {
        return LocationHolder.LOCATION_TAG;
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

    public static void enable() {
        KarmaMetricHolder.getReporterDaemon().start();
    }

    public static void enableKafkaReporter() {
        KarmaMetricHolder.getReporterDaemon().addReporter(new KafkaReporter());
    }

    public static void enableHolderReporter() {
        KarmaMetricHolder.getReporterDaemon().addReporter(
                KarmaMetricHolder.getMetricHolder().reporter());
    }
    public static void enableDWMetricReporter() {
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

