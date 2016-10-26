/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
package com.duitang.service.karma.demo.cluster;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.LoggerFactory;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.boot.KarmaServerConfig;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.IOBalanceFactory;
import com.duitang.service.karma.cluster.Finder;
import com.duitang.service.karma.demo.cluster.NamedMockRPCNode.Perf;
import com.duitang.service.karma.server.AsyncRegistryWriter;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.trace.TraceBlock;
import com.duitang.service.karma.trace.TraceContextHolder;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
public class TestZKBasedLoadBalance {

	static ExecutorService exec = Executors.newCachedThreadPool();

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		// root = (Logger) LoggerFactory.getLogger(KarmaServerConfig.class);
		// root.setLevel(Level.INFO);

		int threadCount = 3;
		String conn = null;
		if (args.length > 0) {
			threadCount = Integer.parseInt(args[0]);
		}
		if (args.length > 1) {
			conn = args[1];
		} else {
			startLocalZK();
			conn = "localhost:2181";
		}
		Thread.sleep(1200);
		List<NamedMockRPCNode> nodes = initRPCs(threadCount);
		List<String> urls = NamedMockRPCNode.getURLs();
		RPCNodeHashing hashing = RPCNodeHashing.createFromString(urls);
		TraceContextHolder.alwaysSampling();
		com.duitang.service.karma.trace.Finder.enableZipkin("tesing", "http://192.168.1.180:9411");
		// com.duitang.service.karma.trace.Finder.enableConsole(true);
		Finder.enableZKRegistry(conn, urls);

		// register writer
		for (NamedMockRPCNode n : nodes) {
			KarmaServerConfig.clusterAware.registerWrite(n);
		}

		IOBalanceFactory fac = Finder.getRegistry().getFactory();
		IOBalance balancer = fac.createIOBalance(Finder.getRegistry(), hashing);

		runner(threadCount, balancer);

		new CountDownLatch(1).await();
	}

	static List<NamedMockRPCNode> initRPCs(int size) throws KarmaException {
		ArrayList<NamedMockRPCNode> ret = new ArrayList<NamedMockRPCNode>();
		for (int i = 0; i < size; i++) {
			NamedMockRPCNode node = NamedMockRPCNode.create(i);
			node.start();
			ret.add(node);
		}
		return ret;
	}

	static void alterRPCServiceStatus(AsyncRegistryWriter wrt, Set<ModifyItem> items) throws Exception {
		for (ModifyItem item : items) {
			String name = NamedMockRPCNode.getName(item.id);
			Perf perf = NamedMockRPCNode.getPerfPredict(name);
			perf.respMui = item.respMui;
			perf.respTou = item.respTou;
			perf.respOKSample = item.respOKSample;
			perf.sampler = new NormalDistribution(perf.respMui, perf.respTou);

			NamedMockRPCNode node = NamedMockRPCNode.getRPCNode(item.id);
			if (item.startUp) {
				node.created = new Date();
			}
			if (item.shutDown) {
				node.halted = new Date();
			}
			if (item.lost) {
				ConcurrentHashMap<String, RPCService> s = null;
				s = (ConcurrentHashMap<String, RPCService>) getField(wrt, "service", ConcurrentHashMap.class);
				String conn = RPCNodeHashing.getRawConnURL(node.getServiceURL());
				s.remove(conn);
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

	static void console() {

	}

	static void runner(int count, IOBalance balancer) {
		for (int i = 0; i < count; i++) {
			MockRunner runner = new MockRunner(balancer);
			exec.submit(runner);
		}
	}
}

class ModifyItem {
	int id;
	double respMui;
	double respTou;
	double respOKSample;

	boolean startUp;
	boolean shutDown;
	boolean lost;
}

class MockRunner implements Runnable {

	Random rnd = new Random();
	protected IOBalance bala;

	public MockRunner(IOBalance balancer) {
		bala = balancer;
	}

	@Override
	public void run() {
		while (true) {
			String url = bala.next(null);
			TraceBlock tb = new TraceBlock();
			tb.tc.group = "testing";
			try {
				NamedMockRPCNode node = NamedMockRPCNode.getRPCNode(url);
				MockResponse resp = node.getResponse();
				tb.tc.props.put("url", resp.url);
				tb.tc.props.put("elapsed", String.valueOf(resp.elapsed));
				tb.tc.props.put("has_error", String.valueOf(resp.error));
				// mock response
				Thread.sleep(resp.elapsed);
//				System.err.println("......... " + resp.elapsed);
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				bala.traceFeed(url, tb.tc);
				KarmaServerConfig.tracer.visit(tb.tc);
				try {
					tb.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(50 * rnd.nextInt(5));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}