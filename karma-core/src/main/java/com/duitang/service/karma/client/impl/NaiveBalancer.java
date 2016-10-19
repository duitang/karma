package com.duitang.service.karma.client.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

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

	static Random iid = new Random();
	protected RPCNodeHashing urls;

	public NaiveBalancer(List<String> urls) {
		this.urls = RPCNodeHashing.createFromString(urls);
	}

	public NaiveBalancer(RPCNodeHashing urls) {
		this.urls = urls;
	}

	@Override
	public String next(String token) {
		RPCNodeHashing urls = this.urls;
		int sz = urls.getNodes().size();
		if (sz == 0) {
			throw new RuntimeException("Not initialized properly!");
		}
		// ignore this token, just next
		int idx = iid.nextInt(sz);
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

}
