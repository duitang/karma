/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.client.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.client.BalancePolicy;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.support.RPCNode;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.support.RegistryInfo;
import com.duitang.service.karma.trace.TraceBlock;
import com.duitang.service.karma.trace.TraceCell;

/**
 * <pre>
 * abstract base balancer Traceable
 * </pre>
 * 
 * @author laurence
 * @since 2016年9月29日
 *
 */
public abstract class TraceableBalancer implements IOBalance {

	public static final String KEY = "traceload";
	static final String myName = TraceableBalancer.class.getName();

	volatile protected NodesAndPolicy nap;

	final public static ConcurrentLinkedQueue<AsyncRegistryReader> configs = new ConcurrentLinkedQueue<>();

	/**
	 * check if hit checkpoint
	 * 
	 * @return
	 */
	abstract boolean hitPoint();

	/**
	 * increase 1 count
	 * 
	 * @param token
	 * @return
	 */
	abstract int count1(String token);

	/**
	 * before commit nodes
	 */
	protected volatile RegistryInfo staging;

	/**
	 * use this to keep nodes validation
	 * 
	 * @param nodes
	 * @return
	 */
	// public static RPCNodeHashing getSafeNodes(LinkedHashMap<String, Double>
	// nodes) {
	// List<String> keys = new ArrayList<String>();
	// for (Entry<String, Double> en : nodes.entrySet()) {
	// keys.add(en.getKey());
	// }
	// return new RPCNodeHashing(keys);
	// }

	public TraceableBalancer(List<String> urls) {
		this(RPCNodeHashing.createFromString(urls));
	}

	public TraceableBalancer(RPCNodeHashing urls) {
		this.staging = new RegistryInfo(false, urls);
		syncReload();
		for (AsyncRegistryReader cfg : configs) {
			try {
				cfg.register(this);
			} catch (KarmaException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String next(String token) {
		NodesAndPolicy n = nap;
		int idx = n.policy.sample();
		String ret = n.hashing.getURLs().get(idx);
		n.updateLoad(ret, 1);
		count1(ret);
		return ret;
	}

	@Override
	public void traceFeed(String token, TraceCell tc) {
		NodesAndPolicy n = nap;
		Integer idx = n.hashing.getURLs().indexOf(token);
		if (idx != null) {
			n.updateLoad(token, -1);
			n.policy.updateResponse(idx, tc.duration * 0.000001, tc.successful);
		}
		// maybe checkpoint
		if (hitPoint()) {
			// hit TraceVisitor here
			checkpoint(n);
		}
	}

	/**
	 * overwrite checkpoint for your jobs
	 * 
	 * @param n
	 */
	protected void checkpoint(NodesAndPolicy n) {
		// just wrapper method
		TraceBlock ts = new TraceBlock(myName, KEY);
		n.policy.updateLoad(n.fetchLoads());
		ts.tc.props.put("nodes", n.hashing.getNodes().toString());
		ts.tc.props.put("old_samples", Arrays.toString(n.policy.getWeights()));
		ts.tc.props.put("old_statistics", Arrays.toString(n.policy.getDebugInfo()));
		// try syncReload first
		if (!syncReload()) {
			n.policy.checkpoint();
		}
		n = nap;
		ts.tc.props.put("new_samples", Arrays.toString(n.policy.getWeights()));
		ts.tc.props.put("new_statistics", Arrays.toString(n.policy.getDebugInfo()));
		try {
			ts.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized protected boolean syncReload() {
		RegistryInfo stage = staging;

		if (stage == null) { // no update
			return false;
		}

		BalancePolicy policy = null;
		Map<String, AtomicInteger> load = new HashMap<>();

		RPCNode n = null;
		double[] samples = new double[stage.getURLs().size()];
		for (int ii = 0; ii < stage.getURLs().size(); ii++) {
			n = stage.getNodes().get(ii);
			samples[ii] = n.getSafeLoad(1.0d);
			load.put(n.url, new AtomicInteger(0));
		}

		if (stage.isFreezeMode()) {
			policy = new FixedPolicy(samples);
		} else {
			policy = new AutoReBalance(stage.getURLs().size());
		}

		NodesAndPolicy ret = new NodesAndPolicy();
		ret.hashing = stage.getHashing();
		ret.policy = policy;
		ret.load = load;
		nap = ret; // only point for overwrite nap
		this.staging = null;
		return true;
	}

	@Override
	public void setNodes(List<String> nodes) {
		if (nodes != null && nap.diffNodes(RPCNodeHashing.createFromString(nodes))) {
			RPCNodeHashing hashing = RPCNodeHashing.createFromString(nodes);
			this.staging = new RegistryInfo(false, hashing);
			// syncReload();
		}
	}

	@Override
	public void setNodesWithWeights(LinkedHashMap<String, Double> nodes) {
		if (nodes != null) {
			RPCNodeHashing hashing = RPCNodeHashing.createFromHashMap(nodes);
			this.staging = new RegistryInfo(true, hashing);
			// syncReload();
		}
	}

}

class NodesAndPolicy {

	BalancePolicy policy;
	RPCNodeHashing hashing;
	// Map<String, Integer> idx;
	Map<String, AtomicInteger> load;
	boolean suspend;

	void updateLoad(String token, int val) {
		AtomicInteger lock = load.get(token);
		if (lock != null) {
			lock.addAndGet(val);
		}
	}

	double[] fetchLoads() {
		double[] ret = new double[load.size()];
		for (int i = 0; i < hashing.getNodes().size(); i++) {
			ret[i] = load.get(hashing.getNodes().get(i)).get();
			ret[i] = ret[i] > 0 ? ret[i] : Candidates.VERY_TRIVIA;
		}
		return ret;
	}

	boolean diffNodes(RPCNodeHashing all) {
		return hashing.compareTo(all) != 0;
	}

}
