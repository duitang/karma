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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.duitang.service.karma.client.BalancePolicy;
import com.duitang.service.karma.client.IOBalance;
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

	public TraceableBalancer(List<String> urls) {
		setNodes(urls);
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
			TraceBlock ts = new TraceBlock(myName, KEY);
			n.policy.updateLoad(n.fetchLoads());
			ts.tc.props.put("nodes", n.nodes.toString());
			ts.tc.props.put("old_samples", Arrays.toString(n.policy.getWeights()));
			ts.tc.props.put("old_statistics", Arrays.toString(n.policy.getStats()));
			n.policy.checkpoint();
			n = nap;
			ts.tc.props.put("new_samples", Arrays.toString(n.policy.getWeights()));
			ts.tc.props.put("new_statistics", Arrays.toString(n.policy.getStats()));
			try {
				ts.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	synchronized public void setNodes(List<String> nodes) {
		if (nodes != null) {
			NodesAndPolicy ret = new NodesAndPolicy();
			nodes =  new ArrayList<String>(new HashSet<String>(nodes));
			ret.nodes = nodes;
			ret.policy = new AutoReBalance(nodes.size());
			ret.idx = new HashMap<String, Integer>();
			ret.load = new HashMap<>();
			for (int i = 0; i < nodes.size(); i++) {
				ret.idx.put(nodes.get(i), i);
				ret.load.put(nodes.get(i), new AtomicInteger(0));
			}
			nap = ret;
		}
	}

}

class NodesAndPolicy {

	BalancePolicy policy;
	List<String> nodes;
	Map<String, Integer> idx;
	Map<String, AtomicInteger> load;

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

}
