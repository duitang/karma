/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
package com.duitang.service.karma.cluster;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.support.RegistryInfo;

/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
public class ZKClientListener implements AsyncRegistryReader {

	final ConcurrentHashMap<IOBalance, Object> members = new ConcurrentHashMap<>();

	protected ZKClusterWorker worker;
	volatile RegistryInfo snap; // freezing mode when deployment

	public void setWorker(ZKClusterWorker worker) {
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
		RegistryInfo ret = worker.refreshRPCNodes();
		if (ret == null) {
			return null; // maybe error in ZK pull
		}
		if (ret.isFreezeMode()) {
			return ret; // freezing mode force return
		}
		if (snap == null) {
			snap = ret;
			return ret; // first pull
		}
		if (snap.getHashing().equals(ret.getHashing())) {
			return null; // no change
		}
		snap = ret; // changed
		return ret;
	}

}
