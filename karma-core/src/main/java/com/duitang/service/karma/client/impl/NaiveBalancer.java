package com.duitang.service.karma.client.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.support.RPCNodeHashing;
import com.duitang.service.karma.trace.TraceCell;

/**
 * <pre>
 * Wait-Free IID round-robin sampler
 * Only for Testing
 * </pre>
 * 
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class NaiveBalancer implements IOBalance {

	static protected Random iid = new Random();
	protected RPCNodeHashing urls;

	public NaiveBalancer(List<String> urls) {
		this.urls = RPCNodeHashing.createFromString(urls);
	}

	public NaiveBalancer(RPCNodeHashing urls) {
		this.urls = urls;
	}

	@Override
	public String next(String token) {
		int sz = urls.getNodes().size();
		if (sz == 0) {
			throw new RuntimeException("Not initialized properly!");
		}
		// ignore this token, just next
		int idx = Math.abs(iid.nextInt()) % sz;
		return urls.getURLs().get(idx);
	}

	@Override
	public void traceFeed(String token, TraceCell tc) {
		// ignore
	}

	@Override
	public void setNodes(List<String> nodes) {
		this.urls = RPCNodeHashing.createFromString(nodes);
	}

	@Override
	public void setNodesWithWeights(LinkedHashMap<String, Double> nodes) {
		this.urls = RPCNodeHashing.createFromHashMap(nodes);
	}

	public static List<String> getSafeNodes(LinkedHashMap<String, Double> nodes) {
		List<String> keys = new ArrayList<String>();
		for (Entry<String, Double> en : nodes.entrySet()) {
			keys.add(en.getKey());
		}
		return getSafeNodes(keys);
	}

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
			if (!ns.contains(s)) {
				ret.add(s);
				ns.add(s);
			}
		}
		return ret;
	}

}
