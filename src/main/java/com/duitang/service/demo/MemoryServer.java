package com.duitang.service.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.avro.AvroRemoteException;

import com.duitang.service.base.MetricCenter;
import com.duitang.service.base.ServerBootstrap;

public class MemoryServer {

	final static String[] PARAMETER_KEYS = { "server", "client", "port", "host", "print", "thread", "loop", "msg" };

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

		MemoryCacheService impl = new MemoryCacheService();
		ServerBootstrap boot = new ServerBootstrap();
		try {
			boot.startUp(DemoService.class, impl, p);
		} catch (IOException e) {
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
		int m = Integer.valueOf(msg);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m; i++) {
			sb.append("1");
		}
		msg = sb.toString();

		int t = Integer.valueOf(thread);
		int s = Integer.valueOf(console_print);
		int l = Integer.valueOf(loop);

		MemoryCacheClientFactory fac = new MemoryCacheClientFactory();
		fac.setUrl("http://" + host + ":" + port);

		CountDownLatch latch = new CountDownLatch(t);
		Thread[] ths = new Thread[t];
		for (int i = 0; i < ths.length; i++) {
			ths[i] = new Thread(new LoadRunner(latch, l, msg, fac));
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

}

class LoadRunner implements Runnable {

	protected CountDownLatch latch;
	protected int loop;
	protected String msg;
	protected MemoryCacheClientFactory fac;
	protected String name;

	public LoadRunner(CountDownLatch latch, int loop, String msg, MemoryCacheClientFactory fac) {
		this.latch = latch;
		this.loop = loop;
		this.msg = msg;
		this.fac = fac;
		this.name = Thread.currentThread().getName();
	}

	@Override
	public void run() {
		long ts = System.currentTimeMillis();
		try {
			DemoService cli = null;
			String val = null;
			try {
				cli = fac.create();
				cli.memory_setString(name, msg, 1000000);
			} catch (AvroRemoteException e1) {
				e1.printStackTrace();
			} finally {
				fac.release(cli);
			}
			for (int i = 0; i < loop; i++) {
				try {
					cli = fac.create();
					val = cli.memory_getString(name);
					if (val.length() != msg.length()) {
						throw new Exception("value error: " + val);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					fac.release(cli);
				}
			}
		} finally {
			ts = System.currentTimeMillis() - ts;
			System.out.println(name + " running elapsed: " + ts + "ms with loop=[" + loop + "]");
			this.latch.countDown();
		}
	}
}
