package com.duitang.service.karma.base;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code
 *  MetricCenter.record("com.duitang.example.service.SomeService.methodName", 20); // record in millis
 * }
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
        if(!clientId.isClient()) {
            b.append(".server");
        } else {
            b.append(".client");
        }
        if(failure) {
            return b.append(".").append("failure").toString();
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

	private static class LocationHolder {
		private static String HOSTNAME = genHostname();
        private static String PID = genPid();
        private static String APP_NAME = genAppName();
        private static boolean includePid = false;

		private static String genHostname() {
			String _hostname = null;
			try {
				_hostname = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				logger.error("get hostname error", e);
				_hostname = System.getenv("HOSTNAME");
                if (_hostname == null || _hostname.isEmpty()) {
                    _hostname = "unknown_" + ThreadLocalRandom.current().nextLong(10000, 99999);
                }
			}
			return  _hostname;
		}

        public static String genAppName() {
            String _appName = null;
            try {
                _appName = SystemPropertyUtil.get("app.name");
            } catch (Exception e) {
                logger.error("get APP_NAME error", e);
            }
            if (_appName == null) {
                _appName = "";
            }
            return _appName;
        }

        private static String genPid() {
            String _pid = null;
            try {
                String name = ManagementFactory.getRuntimeMXBean().getName();
                String[] split = StringUtil.split(name, '@');
                if (split.length == 2) {
                    _pid = split[1];
                }
            } catch (Exception e) {
                logger.error("get PID error", e);
            }
            if (_pid == null) {
                _pid = "";
            }
            return _pid;
        }

        private static void enablePid() {
            includePid = true;
        }

        public static volatile String LOCATION;
        public static void resetLocation() {
            StringBuilder b = new StringBuilder();
            b.append(HOSTNAME);
            if (!APP_NAME.isEmpty()) {
                b.append('-').append(APP_NAME);
            }
            if (includePid && !PID.isEmpty()) {
                b.append('-').append(PID);
            }
            LOCATION = b.toString();
        }

        static {
            resetLocation();
        }
	}

    /**
     *  HOSTNAME or randomly generated string
     */
	static public String getHostname() {
		return LocationHolder.HOSTNAME;
	}

    static public void setAppName(String name) {
        if (name == null) {
            throw new NullPointerException("name==null");
        }
        LocationHolder.APP_NAME = name;
        LocationHolder.resetLocation();
    }

    static public void setHostname(String hostname) {
        if (hostname == null) {
            throw new NullPointerException("hostname==null");
        }
        LocationHolder.HOSTNAME = hostname;
        LocationHolder.resetLocation();
    }

    static public void enablePid() {
        LocationHolder.enablePid();
        LocationHolder.resetLocation();
    }

    static public String getLocation() {
        return LocationHolder.LOCATION;
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

