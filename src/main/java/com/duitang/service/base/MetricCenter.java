package com.duitang.service.base;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MetricCenter {

	final public static boolean debug = false;

	static MetricReportDaemon daemon = null;
	static protected String hostname = null;

	public static Map<String, MetricUnit> method_dur = new HashMap<String, MetricUnit>();

	public static void methodMetric(String clientId, String name, long elapse) {
		methodMetric(clientId, name, elapse, false);
	}

	public static void methodMetric(String clientId, String name, long elapse, boolean failure) {
		if (failure) {
			name += "_Failure_";
		}
		MetricUnit hist = method_dur.get(clientId + ":" + name);
		if (hist != null) {
			hist.metric(elapse);
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

	static public void enableConsoleReporter(final int peroid) {
		Reporter console = new Reporter() {
			@Override
			public void report(Map data) {
				System.out.println(data);
			}
		};
		startDaemonOrAdd(peroid, console);
	}

	static public void enableKafkaReporter(Properties config, long peroid) {
		Reporter kafkaReporter = new KafkaJsonReporter(config);
		startDaemonOrAdd(peroid, kafkaReporter);
	}

	synchronized static void startDaemonOrAdd(long peroid, Reporter r) {
		if (daemon != null) {
			daemon.addReporter(r);
			return;
		}
		LinkedList<Reporter> list = new LinkedList<Reporter>();
		list.add(r);
		daemon = new MetricReportDaemon(peroid, list);
		Thread t = new Thread(daemon);
		t.setDaemon(true);
		t.start();
	}

	static class MetricReportDaemon implements Runnable {

		protected long peroid;
		protected List<Reporter> rps = new LinkedList<Reporter>();

		public MetricReportDaemon(long peroid, List<Reporter> lst) {
			this.peroid = peroid;
			if (lst != null) {
				this.rps.addAll(lst);
			}
		}

		public void addReporter(Reporter r) {
			if (r != null) {
				this.rps.add(r);
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(peroid * 1000);
				} catch (Exception e) {
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
