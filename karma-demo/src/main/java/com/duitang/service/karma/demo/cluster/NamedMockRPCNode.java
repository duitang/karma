/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
package com.duitang.service.karma.demo.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.router.Router;
import com.duitang.service.karma.server.RPCService;

/**
 * @author laurence
 * @since 2016年10月25日
 *
 */
public class NamedMockRPCNode implements RPCService {

	static int base_port = 8000;
	static String nmPrefix = "MOCK_SERVICE";

	static class Perf {
		double respMui = 0.060d * 1000; // ms
		double respTou = 1;
		double respOKSample = 0.9999d;
		NormalDistribution sampler = new NormalDistribution(respMui, respTou);
	}

	static Perf defaultPerf = new Perf();
	static ConcurrentHashMap<String, Perf> nodesPerf = new ConcurrentHashMap<>();
	static ConcurrentHashMap<Integer, NamedMockRPCNode> nodes = new ConcurrentHashMap<>();
	static ConcurrentHashMap<String, NamedMockRPCNode> nodes2 = new ConcurrentHashMap<>();

	String name;
	Date created;
	Date up;
	Date halted;
	String grp;
	Random rnd;

	static public Perf getPerfPredict(String url) {
		Perf p = nodesPerf.get(url);
		if (p == null) {
			p = defaultPerf;
			throw new RuntimeException("using default error!");			
		}
		return p;
	}

	static public void putPerfPredict(String url, Perf perf) {
		nodesPerf.put(url, perf);
	}

	static public List<String> getURLs() {
		ArrayList<String> ret = new ArrayList<String>();
		for (NamedMockRPCNode node : nodes.values()) {
			ret.add(node.getName());
		}
		return ret;
	}

	static public NamedMockRPCNode getRPCNode(Integer idx) {
		return nodes.get(idx);
	}

	static public NamedMockRPCNode getRPCNode(String url) {
		return nodes2.get(url);
	}

	static public String getName(int id) {
		return nmPrefix + "_" + id + ":" + (base_port + id);
	}

	static public NamedMockRPCNode create(int id) {
		NamedMockRPCNode ret = new NamedMockRPCNode(getName(id));
		nodes.put(id, ret);
		nodes2.put(ret.name, ret);
		return ret;
	}

	public String getName() {
		return this.name;
	}

	private NamedMockRPCNode(String name) {
		this.name = name;
		this.created = new Date();
		this.rnd = new Random(System.currentTimeMillis() * Math.round(Math.random()));
	}

	@Override
	public void start() throws KarmaException {
		up = new Date();
	}

	@Override
	public void stop() {
		halted = new Date();
	}

	@Override
	public void setRouter(Router router) {
		// useless, discard
	}

	@Override
	public void setGroup(String grp) {
		this.grp = grp;
	}

	@Override
	public Date getUptime() {
		return created;
	}

	@Override
	public String getGroup() {
		return grp;
	}

	@Override
	public String getServiceURL() {
		return "tcp://" + name;
	}

	@Override
	public String getServiceProtocol() {
		return "tcp";
	}

	@Override
	public boolean online() {
		return halted == null || (halted.getTime() > System.currentTimeMillis());
	}

	public MockResponse getResponse() {
		MockResponse ret = new MockResponse();
		ret.url = getServiceURL();
		Perf perf = getPerfPredict(getName());
		ret.elapsed = Math.abs(Math.round(perf.sampler.sample()));
		ret.error = !(rnd.nextDouble() < perf.respOKSample);
		return ret;
	}

}
