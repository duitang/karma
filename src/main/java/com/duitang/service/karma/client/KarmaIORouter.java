package com.duitang.service.karma.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KarmaIORouter implements IOBalance {

	protected AtomicInteger iid;
	protected List<String> urls;
	protected int sz;

	public KarmaIORouter(List<String> urls) {
		this.iid = new AtomicInteger(0);
		this.urls = new ArrayList<String>(urls);
		this.sz = this.urls.size();
	}

	@Override
	public String next(String token) {
		// ignore this token, just next
		int idx = Math.abs(iid.getAndIncrement()) % sz;
		return urls.get(idx);
	}

}
