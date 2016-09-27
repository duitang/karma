package com.duitang.service.karma.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.duitang.service.karma.client.IOBalance;
import com.duitang.service.karma.trace.TraceCell;

/**
 * Wait-Free IID round-robin sampler
 * 
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class RoundRobinRouter implements IOBalance {

	static protected Random iid = new Random();
	protected List<String> urls;
	protected int sz;

	public RoundRobinRouter() {
		this.urls = Collections.EMPTY_LIST;
		this.sz = urls.size();
	}

	public RoundRobinRouter(List<String> urls) {
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

	}

	@Override
	public void setNodes(List<String> nodes) {
		this.urls = new ArrayList<String>(nodes);
	}

}
