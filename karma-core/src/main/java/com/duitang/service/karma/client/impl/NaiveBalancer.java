package com.duitang.service.karma.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.duitang.service.karma.client.IOBalance;
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
	protected List<String> urls;
	protected int sz;

	public NaiveBalancer() {
		this.urls = Collections.EMPTY_LIST;
		this.sz = urls.size();
	}

	public NaiveBalancer(List<String> urls) {
		this.urls = new ArrayList<String>(urls);
		this.sz = this.urls.size();
	}

	@Override
	public String next(String token) {
		if (sz == 0) {
			throw new RuntimeException("Not initialized properly!");
		}
		// ignore this token, just next
		int idx = Math.abs(iid.nextInt()) % sz;
		return urls.get(idx);
	}

	@Override
	public void traceFeed(String token, TraceCell tc) {
		// ignore
	}

	@Override
	public void setNodes(List<String> nodes) {
		this.urls = getSafeNodes(nodes);
	}

	@Override
	public void setNodesWithWeights(LinkedHashMap<String, Double> nodes) {
		this.urls = getSafeNodes(nodes);
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
