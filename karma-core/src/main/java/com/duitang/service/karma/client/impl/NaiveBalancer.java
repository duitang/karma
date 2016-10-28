package com.duitang.service.karma.client.impl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.support.NodeDD;
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
	protected AtomicLong[] count;

	public NaiveBalancer(List<String> urls) {
		this(RPCNodeHashing.createFromString(urls));
	}

	public NaiveBalancer(RPCNodeHashing urls) {
		reload(urls);
	}

	void reload(RPCNodeHashing urls) {
		this.urls = urls;
		count = new AtomicLong[urls.getNodes().size()];
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
		count[idx].incrementAndGet();
		return urls.getURLs().get(idx);
	}

	@Override
	public void traceFeed(String token, TraceCell tc) {
		// ignore
	}

	@Override
	public void setNodes(List<String> nodes) {
		reload(RPCNodeHashing.createFromString(nodes));
	}

	@Override
	public void setNodesWithWeights(LinkedHashMap<String, Float> nodes) {
		reload(RPCNodeHashing.createFromHashMap(nodes));
	}

	@Override
	public String getDebugInfo() {
		NodeDD ret = new NodeDD();
		ret.setAttr("policy", NaiveBalancer.class.getName());
		ret.setAttr("urls", urls.getURLs());
		ret.setAttr("rpcload", Arrays.toString(count));
		return ret.toString();
	}

}
