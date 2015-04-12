package com.duitang.service.karma.base;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MetricCenter {

	final static Logger out = Logger.getLogger(MetricCenter.class);
	final public static boolean debug = false;

	final static String[] NOT_IN_PACKAGE_NAME = { "com.duitang.service.karma" };// "com.duitang.webx",

	static MetricReportDaemon daemon = null;
	static protected String hostname = null;
	public static Map<String, MetricUnit> method_dur = new HashMap<String, MetricUnit>();

	final static Reporter console = new Reporter() {
		@Override
		public void report(Map data) {
			System.out.println(data);
		}
	};

	static {
		daemon = new MetricReportDaemon();
		Thread t = new Thread(daemon);
		t.setDaemon(true);
		t.start();
	}

	public static void methodMetric(String clientId, String name, long elapse) {
		methodMetric(clientId, name, elapse, false);
	}

	public static void methodMetric(String clientId, String name, long elapse, boolean failure) {
		if (failure) {
			name += "_Failure_";
		}
		MetricUnit hist = method_dur.get(clientId + ":" + name);
		if (hist != null) {
			hist.record(elapse);
		}
	}

	static public String getHostname() {
		if (hostname != null) {
			return hostname;
		}
		String ret;
		try {
			ret = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			ret = System.getenv("HOSTNAME");
		}
		return ret;
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

	public static void initMetric(Class clazz, String clientid) {
		String nm;
		String nmf;
		for (Method m : clazz.getMethods()) {
			nm = clientid + ":" + m.getName();
			nmf = nm + "_Failure_";
			if (debug) {
				System.err.println(" ------> " + nm);
			}

			// maybe racing, but not serious problem
			if (!MetricCenter.method_dur.containsKey(nm)) {
				MetricCenter.method_dur.put(nm, new MetricUnit(clientid, m.getName(), "OK"));
			}
			// maybe racing, but not serious problem
			if (!MetricCenter.method_dur.containsKey(nmf)) {
				MetricCenter.method_dur.put(nmf, new MetricUnit(clientid, m.getName(), "ERR"));
			}
		}
	}

	static public void alterReportPeroid(long peroid) {
		daemon.peroid = peroid;
	}

	static public void enableConsoleReporter(boolean openit) {
		if (openit) {
			daemon.rps.add(console);
		} else {
			daemon.rps.remove(console);
		}
	}

	static public void enableKafkaReporter(Properties config) {
		Reporter kafkaReporter = new KafkaJsonReporter(config);
		daemon.rps.add(kafkaReporter);
	}

	static public void addLoggerReporter(final Logger logR) {
		Reporter logReport = new Reporter() {
			@Override
			public void report(Map data) {
				logR.info(data.toString());
			}
		};
		daemon.rps.add(logReport);
	}

	static public void addLoggerReporterByName(String name) {
		final Logger logger = Logger.getLogger(name);
		logger.setLevel(Level.INFO);
		Reporter logReport = new Reporter() {
			Logger logR = logger;

			@Override
			public void report(Map data) {
				logR.info(data.toString());
			}
		};
		daemon.rps.add(logReport);
	}

	static class MetricReportDaemon implements Runnable {

		protected long peroid = 10; // 10 sencods
		protected Set<Reporter> rps = Collections.synchronizedSet(new HashSet<Reporter>());

		public void addReporter(Reporter r) {
			if (r != null) {
				this.rps.add(r);
			}
		}

		public void setPeroid(long peroid) {
			this.peroid = peroid;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(peroid * 1000);
				} catch (Exception e) {
				}
				if (rps.isEmpty()) {
					continue;
				}
				Map data;
				for (MetricUnit u : method_dur.values()) {
					try {
						try {
							data = u.sample();
							data.put("client_id", u.clientId);
							data.put("name", u.name);
							data.put("group", u.group);
						} catch (Exception e) {
							continue;
						}
						for (Reporter r : rps) {
							r.report(data);
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					} finally {
						data = null;
					}
				}
			}
		}
	}
}

interface Reporter {
	void report(Map data);
}
