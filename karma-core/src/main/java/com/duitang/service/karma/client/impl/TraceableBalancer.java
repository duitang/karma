/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.client.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.client.AsyncRegistryReader;
import com.duitang.service.karma.client.BalancePolicy;
import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.support.NodeDD;
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
	protected AtomicLong checkpointVer = new AtomicLong(0);
	protected AtomicLong reloadVer = new AtomicLong(0);

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

	public TraceableBalancer(List<String> urls) {
		this(RPCNodeHashing.createFromString(urls));
	}

	public TraceableBalancer(RPCNodeHashing urls) {
		init(urls);
	}

	protected boolean init(RPCNodeHashing urls) {
		if (urls == null) {
			return false;
		}
		this.staging = new RegistryInfo(false, urls);
		syncReload();
		for (AsyncRegistryReader cfg : configs) {
			try {
				cfg.register(this);
			} catch (KarmaException e) {
				e.printStackTrace();
			}
		}
		return true;
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
			if (tc != null) {
				n.policy.updateResponse(idx, tc.duration * 0.000001f, tc.successful);
			}
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
		// try syncReload first
		if (!syncReload()) {
			n.policy.checkpoint();
			checkpointVer.incrementAndGet();
		}
		ts.tc.props.putAll(getNodeDD(n).getAtrrs());
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

		RPCNode n = null;
		float[] samples = new float[stage.getURLs().size()];
		for (int ii = 0; ii < stage.getURLs().size(); ii++) {
			n = stage.getNodes().get(ii);
			samples[ii] = n.getSafeLoad(1.0f);
		}

		if (stage.isFreezeMode()) {
			policy = new FixedPolicy(samples);
		} else {
			AutoReBalance pl = new AutoReBalance(stage.getURLs().size());
			List<Double> dc = stage.getHashing().getDecays();
			pl.cdd.decay = new float[dc.size()];
			for (int i = 0; i < pl.cdd.decay.length; i++) {
				pl.cdd.decay[i] = dc.get(i) == null ? Float.MAX_VALUE : Double.valueOf(dc.get(i)).floatValue();
			}
			policy = pl;
		}

		NodesAndPolicy ret = new NodesAndPolicy();
		ret.hashing = stage.getHashing();
		ret.policy = policy;
		nap = ret; // only point for overwrite nap
		reloadVer.incrementAndGet();
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
	public void setNodesWithWeights(LinkedHashMap<String, Float> nodes) {
		if (nodes != null) {
			RPCNodeHashing hashing = RPCNodeHashing.createFromHashMap(nodes);
			this.staging = new RegistryInfo(true, hashing);
			// syncReload();
		}
	}

	public String getDebugInfo() {
		return getNodeDD(nap).toString();
	}

	protected NodeDD getNodeDD(NodesAndPolicy n) {
		NodeDD ret = new NodeDD();
		ret.setAttr("policy", this.getClass().getName());
		// stupid zipkin's json array support, so using loop
		// ret.setAttr("nodes", n.policy.getDebugInfo());
		String[] d = n.policy.getDebugInfo();
		for (int i = 0; i < d.length; i++) {
			ret.setAttr("node[" + i + "]", d[i]);
		}
		ret.setAttr("urls", n.hashing.getURLs());
		ret.setAttr("rpcload", n.policy.getLoads());
		ret.setAttr("checkpoint_version", checkpointVer.get());
		ret.setAttr("reload_version", reloadVer.get());
		ret.setAttr("decays", nap.hashing.getDecays());
		return ret;
	}

}

class NodesAndPolicy {

	BalancePolicy policy;
	RPCNodeHashing hashing;

	void updateLoad(String token, int val) {
		int idx = hashing.getURLs().indexOf(token);
		policy.updateLoad(idx, val);
	}

	boolean diffNodes(RPCNodeHashing all) {
		return hashing.compareTo(all) != 0;
	}

}
