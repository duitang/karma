/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
package com.duitang.service.karma.demo.cluster;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.eclipse.jetty.server.Server;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.client.impl.TraceableBalancer;
import com.duitang.service.karma.cluster.Finder;
import com.duitang.service.karma.cluster.ZKClusterWorker;
import com.duitang.service.karma.cluster.ZKServerRegistry;
import com.duitang.service.karma.demo.cluster.NamedMockRPCNode.Perf;
import com.duitang.service.karma.server.AsyncRegistryWriter;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.trace.TraceBlock;
import com.duitang.service.karma.trace.TraceContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
public class TestZKBasedLoadBalance {

	static ExecutorService exec = Executors.newCachedThreadPool();
	static Map<String, AtomicInteger> counter = new HashMap<String, AtomicInteger>();

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		// root = (Logger) LoggerFactory.getLogger(KarmaServerConfig.class);
		// root.setLevel(Level.INFO);

		TestArgs cfg = initArgs(args);

		int threadCount = cfg.threadCount;
		System.err.println("using threads = " + threadCount);
		int nodesCount = cfg.nodesCount;
		System.err.println("using nodes = " + nodesCount);
		String zk = cfg.zk;
		System.err.println("using zookeeper = " + zk);
		String zipkin = cfg.zipkin;
		System.err.println("using zipkin = " + zipkin);

		if (zk.contains("localhost")) {
			startLocalZK();
		}
		Thread.sleep(1200);
		List<NamedMockRPCNode> nodes = initRPCs(nodesCount);
		List<String> urls = NamedMockRPCNode.getURLs();
		RPCNodeHashing hashing = RPCNodeHashing.createFromString(urls);
		TraceContextHolder.alwaysSampling();
		if (zipkin != null) {
			com.duitang.service.karma.trace.Finder.enableZipkin("tesing", zipkin);
			com.duitang.service.karma.trace.Finder.enableConsole(false);
		} else {
			com.duitang.service.karma.trace.Finder.enableConsole(true);
		}

		Finder.enableZKRegistry(zk, urls);

		// register writer
		for (NamedMockRPCNode n : nodes) {
			KarmaServerConfig.clusterAware.registerWrite(n);
		}

		IOBalanceFactory fac = Finder.getRegistry().getFactory();
		IOBalance balancer = fac.createIOBalance(Finder.getRegistry(), hashing);

		System.err.println("using IOBalance: " + balancer.getClass().getName());
		System.err.println("using IOBalance: " + ((TraceableBalancer) balancer).getDebugInfo());

		runner(threadCount, balancer);

		console(zk);

		new CountDownLatch(1).await();
	}

	static List<NamedMockRPCNode> initRPCs(int size) throws KarmaException {
		ArrayList<NamedMockRPCNode> ret = new ArrayList<NamedMockRPCNode>();
		for (int i = 0; i < size; i++) {
			NamedMockRPCNode node = NamedMockRPCNode.create(i);
			node.start();
			ret.add(node);
			counter.put(node.name, new AtomicInteger(0));
			Perf perf = new Perf();
			NamedMockRPCNode.putPerfPredict(node.getName(), perf);
		}
		return ret;
	}

	static void alterRPCServiceStatus(AsyncRegistryWriter wrt, Set<ModifyItem> items) throws Exception {
		for (ModifyItem item : items) {
			String name = NamedMockRPCNode.getName(item.id);
			Perf perf = NamedMockRPCNode.getPerfPredict(name);
			perf.respMui = item.mean;
			perf.respTou = item.u;
			perf.respOKSample = item.ok;
			perf.sampler = new NormalDistribution(perf.respMui, perf.respTou);

			NamedMockRPCNode node = NamedMockRPCNode.getRPCNode(item.id);
			if (item.begin) {
				node.created = new Date();
			}
			if (item.end) {
				node.halted = new Date();
			}
			if (item.lost) {
				ConcurrentHashMap<String, RPCService> s = null;
				s = (ConcurrentHashMap<String, RPCService>) getField(wrt, "service", ConcurrentHashMap.class);
				String conn = RPCNodeHashing.getRawConnURL(node.getServiceURL());
				s.remove(conn);
			}
			for (Perf p : NamedMockRPCNode.nodesPerf.values()) {
				System.out.println("Perf: " + p.respMui + "," + p.respTou + "," + p.respOKSample);
			}
		}
	}

	static void startLocalZK() throws Exception {
		ZKEmbed.start();
	}

	static <T> T getField(Object src, String f, Class<T> type) throws Exception {
		Field field = src.getClass().getDeclaredField(f);
		field.setAccessible(true);
		return (T) field.get(src);
	}

	static <T> T getField2(Class clz, String f, Class<T> type) throws Exception {
		Field field = clz.getDeclaredField(f);
		field.setAccessible(true);
		return (T) field.get(null);
	}

	static void console(String zk) throws Exception {
		ConcurrentHashMap<String, ZKClusterWorker> m = getField2(ZKClusterWorker.class, "owner",
				ConcurrentHashMap.class);
		ZKClusterWorker w0 = m.get(zk);
		ZKServerRegistry w1 = getField(w0, "zkSR", ZKServerRegistry.class);
		Command cmd = new Command(w1);
		Server server = new Server(8080);
		server.setHandler(new ZKNodeAlterHandler(cmd));
		server.start();
		server.join();
	}

	static void runner(int count, IOBalance balancer) {
		for (int i = 0; i < count; i++) {
			MockRunner runner = new MockRunner(balancer);
			runner.name = "runner-" + i;
			exec.submit(runner);
		}
	}

	static TestArgs initArgs(String[] args) {
		TestArgs ret = new TestArgs();
		CmdLineParser parser = new CmdLineParser(ret);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			e.printStackTrace();
		}
		return ret;
	}
}

class Command {

	final static ObjectMapper mapper = new ObjectMapper();

	AsyncRegistryWriter writer;

	public Command(AsyncRegistryWriter w) {
		writer = w;
	}

	public String update(String s) {
		ModifyItem[] p = null;
		if (p == null) {

			try {
				ModifyItem obj = mapper.readValue(s, ModifyItem.class);
				p = new ModifyItem[] { obj };
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		if (p == null) {
			try {
				ModifyItem[] objs = mapper.readValue(s, ModifyItem[].class);
				p = objs;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (p != null) {
			try {
				TestZKBasedLoadBalance.alterRPCServiceStatus(writer, new HashSet(Arrays.asList(p)));
				return "OK";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return s + "  ==> read object error";
	}

	public String show() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (Entry<String, AtomicInteger> item : TestZKBasedLoadBalance.counter.entrySet()) {
			sb.append("\"").append(item.getKey()).append("\":").append(item.getValue()).append(",");
		}
		sb.deleteCharAt(sb.length() - 1).append("}");
		return sb.toString();
	}

}

class ModifyItem {
	public int id;
	public double mean;
	public double u;
	public double ok;

	public boolean begin;
	public boolean end;
	public boolean lost;
}

class MockRunner implements Runnable {

	Random rnd = new Random();
	IOBalance bala;
	AtomicLong lcount = new AtomicLong();
	String name;

	public MockRunner(IOBalance balancer) {
		bala = balancer;
	}

	@Override
	public void run() {
		while (true) {
			String url = bala.next(null);
			TraceBlock tb = new TraceBlock();
			tb.tc.group = "testing";
			boolean err = false;
			try {
				NamedMockRPCNode node = NamedMockRPCNode.getRPCNode(url);
				MockResponse resp = node.getResponse();
				tb.tc.props.put("url", resp.url);
				tb.tc.props.put("elapsed", String.valueOf(resp.elapsed));
				tb.tc.props.put("has_error", String.valueOf(resp.error));
				err = resp.error;
				// mock response
				Thread.sleep(resp.elapsed);
				// System.err.println("......... " + resp.elapsed);
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				try {
					if (err) {
						tb.close(new Exception("mock error happen!"));
					} else {
						tb.close(); // trigger trace report automatic
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// just feedback to IOBalance
				bala.traceFeed(url, tb.tc);
			}

			TestZKBasedLoadBalance.counter.get(url).incrementAndGet();

			long id = lcount.incrementAndGet();
			if (id % 100 == 0) {
				System.err.println(name + " run " + id);
				// System.err.println(TestZKBasedLoadBalance.counter);
			}

			try {
				Thread.sleep(5 * rnd.nextInt(5));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}