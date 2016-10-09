/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
package com.duitang.service.karma.cluster;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.RegistryInfo;

/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
public class ZKClientListener implements AsyncRegistryReader {

	final ConcurrentHashMap<IOBalance, Object> members = new ConcurrentHashMap<>();

	protected CuratorClusterWorker worker;

	public void setWorker(CuratorClusterWorker worker) {
		this.worker = worker;
	}

	@Override
	public void register(IOBalance balancer) throws KarmaException {
		members.put(balancer, "");
	}

	@Override
	public void unregister(IOBalance balancer) throws KarmaException {
		members.remove(balancer);
	}

	public void updateAllNodes(List<String> nodes) {
		HashSet<IOBalance> all = new HashSet<>(members.keySet());
		for (IOBalance balancer : all) {
			balancer.setNodes(nodes);
		}
	}

	public void updateAllNodes(LinkedHashMap<String, Double> nodes) {
		HashSet<IOBalance> all = new HashSet<>(members.keySet());
		for (IOBalance balancer : all) {
			balancer.setNodesWithWeights(nodes);
		}
	}

	@Override
	public RegistryInfo syncPull() {
		RegistryInfo ret = new RegistryInfo();
		ClusterMode mode = worker.syncGetMode();
		if (mode != null && mode.freeze != null && mode.freeze) {
			ret.wNodes = mode.nodes;
			ret.freezeMode = true;
		} else { // not freeze mode
			List<ClusterNode> nodes = worker.syncRead();
			LinkedHashMap<String, Double> ret0 = new LinkedHashMap<>();
			double total = 0;
			double nload = 0d;
			for (ClusterNode n : nodes) {
				if (n.isAlive()) {
					nload = n.load == null ? 1 : n.load;
					total += nload;
					ret0.put(RegistryInfo.getConnectionURL(n.url), nload);
				}
			}
			for (Entry<String, Double> en : ret0.entrySet()) {
				en.setValue(en.getValue() / total);
			}
			ret.wNodes = ret0;
			ret.freezeMode = false;
		}
		return ret;
	}

}
