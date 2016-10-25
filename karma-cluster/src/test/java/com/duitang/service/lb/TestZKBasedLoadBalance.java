/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
package com.duitang.service.lb;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.ZKEmbed;
import com.duitang.service.karma.cluster.ZKClusterWorker;
import com.duitang.service.karma.cluster.ZKServerRegistry;
import com.duitang.service.karma.server.AsyncRegistryWriter;
import com.duitang.service.karma.server.RPCService;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.lb.NamedMockRPCNode.Perf;

/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
public class TestZKBasedLoadBalance {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ZKClusterWorker worker = null;
		String conn = null;
		if (args.length > 0) {
			conn = args[0];
		} else {
			startLocalZK();
			conn = "localhost:2181";
		}
		worker = ZKClusterWorker.createInstance(conn);
		ZKServerRegistry wrt = getField(worker, "zkSR", ZKServerRegistry.class);
		initRPCs(wrt, 3);

		new CountDownLatch(1).await();
	}

	static void initRPCs(AsyncRegistryWriter wrt, int size) throws KarmaException {
		for (int i = 0; i < size; i++) {
			NamedMockRPCNode node = NamedMockRPCNode.create(i);
			node.start();
			wrt.register(node);
		}
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
