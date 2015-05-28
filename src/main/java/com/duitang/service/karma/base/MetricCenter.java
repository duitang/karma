package com.duitang.service.karma.base;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricCenter {
	final static Logger logger = LoggerFactory.getLogger(MetricCenter.class);

	final static String[] NOT_IN_PACKAGE_NAME = { "com.duitang.service.karma" };// "com.duitang.webx",

    private static ConcurrentHashMap<String, MetricUnit> metricUnits = new ConcurrentHashMap<>();

    public static String metricName(ClientId clientId, String method, boolean failure) {
        StringBuilder b = new StringBuilder()
                .append(clientId.getName())
                .append('.')
                .append(method);
        if(!clientId.isClient()) {
            b.append(".server");
        }
        if(failure) {
            return b.append(".").append("fail").toString();
        }
        return b.toString();
    }

    private static MetricUnit metricUnitFor(ClientId clientId, String method, boolean failure) {
        String name = metricName(clientId, method, failure);
        MetricUnit metricUnit = metricUnits.get(name);
        if(metricUnit != null) {
            return metricUnit;
        }
        synchronized (MetricCenter.class) {
            metricUnit = metricUnits.get(name);
            if (metricUnit == null) {
                metricUnit = new MetricUnit(name, failure ? "ERR" : "OK");
                metricUnits.put(name, metricUnit);
            }
        }
        return metricUnit;
    }

    private static MetricUnit metricUnitFor(String name) {
        MetricUnit metricUnit = metricUnits.get(name);
        if (metricUnit != null) {
            return metricUnit;
        }
        synchronized (MetricCenter.class) {
            metricUnit = metricUnits.get(name);
            if(metricUnit == null) {
                metricUnit = new MetricUnit(name);
                metricUnits.put(name, metricUnit);
            }
            return metricUnit;
        }
    }

    public static void record(ClientId clientId, String method, long elapse, boolean failure) {
        metricUnitFor(clientId, method, failure).record(elapse);
    }

    public static void record(String stuff, long elapse) {
        metricUnitFor(stuff).record(elapse);
    }

	private static class HostNameHolder {
		public static final String hostname;
		static {
			String _hostname = null;
			try {
				_hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				logger.error("get hostname error", e);
				_hostname = System.getenv("HOSTNAME");
                if (_hostname == null || _hostname.isEmpty()) {
                    _hostname = "gen-" + ThreadLocalRandom.current().nextLong(10000, 99999);
                }
			}
			hostname = _hostname;
		}
	}


    /**
     *  hostname or randomly generated string
     */
	static public String getHostname() {
		return HostNameHolder.hostname;
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
}

