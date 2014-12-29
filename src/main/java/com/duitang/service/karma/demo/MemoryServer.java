package com.duitang.service.karma.demo;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;

import com.duitang.service.karma.base.ClientFactory;
import com.duitang.service.karma.base.MetricCenter;
import com.duitang.service.karma.base.ServerBootstrap;

public class MemoryServer {

	final static String[] PARAMETER_KEYS = { "server", "client", "port", "host", "print", "thread", "loop", "msg", "map", "one", "verbose", "trace" };

	static protected void reloadLog4J() {
		LogManager.resetConfiguration();
		// DOMConfigurator.configure("conf/test/log4j.xml");
	}

	static protected DemoService one;

	public static void main(String[] args) {
		Map<String, String> param = argsToMap(args);
		if (param.containsKey("client")) {
			runClient(param);
		} else if (!param.containsKey("server")) {
			printUsage();
			System.exit(1);
		} else {
			runServer(param);
		}
	}

	static void runServer(Map<String, String> param) {
		String port = "9999";
		if (param.containsKey("port")) {
			port = param.get("port");
		}
		String console_print = "-1";
		if (param.containsKey("print")) {
			console_print = param.get("print");
		}
		int p = Integer.valueOf(port);
		int s = Integer.valueOf(console_print);
		boolean verbose = false;
		if (param.containsKey("verbose")) {
			verbose = true;
			ConsoleAppender console = new ConsoleAppender();
			console.setWriter(new OutputStreamWriter(System.out));
			console.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
			LogManager.getRootLogger().addAppender(console);
			org.apache.log4j.Logger.getLogger("org.apache.commons.pool2.impl.GenericObjectPool").setLevel(Level.DEBUG);
		}
		String msg = "20000";
		if (param.containsKey("msg")) {
			msg = param.get("msg");
		}
		msg = genMsg(msg);

		MemoryCacheService impl = new MemoryCacheService(verbose);
		ServerBootstrap boot = new ServerBootstrap();
		impl.memory_setString("main", msg, 50000);
		impl.memory_setString("aaa", msg, 50000);
		try {
			boot.addService(DemoService.class, impl);
			boot.addService(DemoJsonRPCService.class, new DemoJsonRPCImpl());
			boot.startUp(p - 1, "");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (s > 0) {
			MetricCenter.enableConsoleReporter(s);
		}
	}

	static void runClient(Map<String, String> param) {
		String port = "9999";
		if (param.containsKey("port")) {
			port = param.get("port");
		}
		String host = "localhost";
		if (param.containsKey("host")) {
			host = param.get("host");
		}
		String console_print = "-1";
		if (param.containsKey("print")) {
			console_print = param.get("print");
		}
		String thread = "10";
		if (param.containsKey("thread")) {
			thread = param.get("thread");
		}
		String loop = "10000";
		if (param.containsKey("loop")) {
			loop = param.get("loop");
		}
		String msg = "20000";
		if (param.containsKey("msg")) {
			msg = param.get("msg");
		}
		msg = genMsg(msg);

		int t = Integer.valueOf(thread);
		int s = Integer.valueOf(console_print);
		int l = Integer.valueOf(loop);

		boolean usemap = false;
		if (param.containsKey("map")) {
			usemap = true;
		}

		boolean one = false;
		if (param.containsKey("one")) {
			one = true;
		}
		int trace = -1; // nouse
		if (param.containsKey("trace")) {
			trace = Integer.valueOf(param.get("trace"));
		}

		if (param.containsKey("verbose")) {
			ConsoleAppender console = new ConsoleAppender();
			console.setWriter(new OutputStreamWriter(System.out));
			console.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
			LogManager.getRootLogger().addAppender(console);
			org.apache.log4j.Logger.getLogger("org.apache.commons.pool2.impl.GenericObjectPool").setLevel(Level.DEBUG);
			org.apache.log4j.Logger.getLogger("error").setLevel(Level.DEBUG);
		}

		ClientFactory<DemoService> fac = ClientFactory.createFactory(DemoService.class);
		fac.setUrl(host + ":" + port);

		CountDownLatch latch = new CountDownLatch(t);
		if (one) {
			// LoadRunner.one = fac.create();
		}
		Thread[] ths = new Thread[t];
		for (int i = 0; i < ths.length; i++) {
			ths[i] = new Thread(new LoadRunner(latch, l, msg, fac, usemap, trace));
			ths[i].start();
		}

		if (s > 0) {
			MetricCenter.enableConsoleReporter(s);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(1);
	}

	static void printUsage() {
		System.out.println("support paramter: ");
		for (String s : PARAMETER_KEYS) {
			System.out.println("    --" + s + "=");
		}
	}

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

	static String genMsg(String msg) {
		int m = Integer.valueOf(msg);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m - 1; i++) {
			sb.append("1");
		}
		sb.append("9");
		return sb.toString();
	}
}

class LoadRunner implements Runnable {

	static protected DemoService one;
	protected CountDownLatch latch;
	protected int loop;
	protected String msg;
	protected ClientFactory<DemoService> fac;
	protected String name;
	protected boolean usemap;
	protected int trace;

	public LoadRunner(CountDownLatch latch, int loop, String msg, ClientFactory<DemoService> fac, boolean usemap, int trace) {
		this.latch = latch;
		this.loop = loop;
		this.msg = msg;
		this.fac = fac;
		this.usemap = usemap;
		this.trace = trace;
		this.name = Thread.currentThread().getName();
	}

	@Override
	public void run() {
		int i = 0;
		int err = 0;
		long ts = System.currentTimeMillis();
		try {
			DemoService cli = null;
			String val = null;
			Map vvv = new HashMap();
			vvv.put("aaa", msg);
			Map v = new HashMap(vvv);
			Map vv = null;
			try {
				cli = fac.create();
				if (usemap) {
					if (!cli.setmap(name, v)) {
						System.err.println("setting error: " + name);
					}
				} else {
					if (!cli.memory_setString(name, msg, 1000000)) {
						System.err.println("setting error: " + name);
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				fac.release(cli);
			}
			for (i = 0; i < loop; i++) {
				try {
					// cli = one;
					// if (one == null) {
					cli = fac.create();
					// }
					if (trace > 0) {
						val = cli.trace_msg(name, trace);
						if (val.length() != name.length()) {
							throw new Exception("value error: " + name);
						}
					} else if (usemap) {
						vv = cli.getmap(name);
						if (!vv.containsKey("aaa") || vv.get("aaa").toString().length() != msg.length()) {
							throw new Exception("value error: " + vv.toString());
						}
					} else {
						val = cli.memory_getString(name);
						// if (val.length() != msg.length()) {
						if (val == null) {
							throw new Exception("value error: " + val);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					err++;
				} finally {
					// if (one == null) {
					fac.release(cli);
					// }
				}
			}
		} finally {
			ts = System.currentTimeMillis() - ts;
			System.out.println(name + " running elapsed: " + ts + "ms with loop=[" + loop + "] @" + i + ", error=" + err);
			this.latch.countDown();
		}
	}
}
