/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.client.BalancePolicy;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.client.RegistryInfo;
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
	protected volatile LinkedHashMap<String, Double> stagingNodes0;
	protected volatile List<String> stagingNodes1;

	/**
	 * use this to keep nodes validation
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<String> getSafeNodes(List<String> nodes) {
		Set<String> ns = new HashSet<String>();
		List<String> ret = new ArrayList<String>();
		for (String s : nodes) {
			s = RegistryInfo.getConnectionURL(s);
			if (!ns.contains(s)) {
				ret.add(s);
				ns.add(s);
			}
		}
		return ret;
	}

	/**
	 * use this to keep nodes validation
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<String> getSafeNodes(LinkedHashMap<String, Double> nodes) {
		List<String> keys = new ArrayList<String>();
		for (Entry<String, Double> en : nodes.entrySet()) {
			keys.add(en.getKey());
		}
		return getSafeNodes(keys);
	}

	public TraceableBalancer(List<String> urls) {
		urls = getSafeNodes(urls);
		// setNodes(urls);
		this.stagingNodes1 = urls;
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
		String ret = n.nodes.get(n.policy.sample());
		n.updateLoad(ret, 1);
		count1(ret);
		return ret;
	}

	@Override
	public void traceFeed(String token, TraceCell tc) {
		NodesAndPolicy n = nap;
		Integer idx = n.idx.get(token);
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
		ts.tc.props.put("nodes", n.nodes.toString());
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
		List<String> newhosts = null;
		BalancePolicy policy = null;
		Map<String, Integer> idx = new HashMap<String, Integer>();
		Map<String, AtomicInteger> load = new HashMap<>();
		if (stagingNodes0 != null) { // 1st freeze mode
			LinkedHashMap<String, Double> n = stagingNodes0;
			newhosts = getSafeNodes(n);
			double[] samples = new double[newhosts.size()];
			for (int i = 0; i < samples.length; i++) {
				samples[i] = n.get(newhosts.get(i));
			}
			policy = new FixedPolicy(samples);
			for (int i = 0; i < n.size(); i++) {
				idx.put(newhosts.get(i), i);
				load.put(newhosts.get(i), new AtomicInteger(0));
			}
		} else if (stagingNodes1 != null) { // 2nd dynamic mode
			List<String> n = stagingNodes1;
			newhosts = getSafeNodes(n);
			policy = new AutoReBalance(n.size());
			for (int i = 0; i < n.size(); i++) {
				idx.put(n.get(i), i);
				load.put(n.get(i), new AtomicInteger(0));
			}
		} else { // nothing happens
			return false;
		}
		NodesAndPolicy ret = new NodesAndPolicy();
		ret.nodes = newhosts;
		ret.policy = policy;
		ret.idx = idx;
		ret.load = load;
		nap = ret; // only point for overwrite nap
		this.stagingNodes0 = null;
		this.stagingNodes1 = null;
		return true;
	}

	@Override
	public void setNodes(List<String> nodes) {
		if (nodes != null && nap.diffNodes(nodes)) {
			this.stagingNodes1 = nodes;
			syncReload();
		}
	}

	@Override
	public void setNodesWithWeights(LinkedHashMap<String, Double> nodes) {
		if (nodes != null) {
			this.stagingNodes0 = nodes;
			syncReload();
		}
	}

}

class NodesAndPolicy {

	BalancePolicy policy;
	List<String> nodes;
	Map<String, Integer> idx;
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
		for (int i = 0; i < nodes.size(); i++) {
			ret[i] = load.get(nodes.get(i)).get();
			ret[i] = ret[i] > 0 ? ret[i] : Candidates.VERY_TRIVIA;
		}
		return ret;
	}

	boolean diffNodes(List<String> all) {
		HashSet<String> n0 = new HashSet<String>(all);
		n0.removeAll(nodes);
		HashSet<String> n1 = new HashSet<String>(nodes);
		n1.removeAll(all);

		boolean ret = true;
		if (n0.size() == n1.size() && n0.size() == 0) {
			ret = false;
		}
		return ret;
	}

}
