/**
 * @author laurence
 * @since 2016年10月9日
 *
 */
package com.duitang.service.karma.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * @author laurence
 * @since 2016年10月9日
 *
 */
public class RPCNodeHashing implements Comparable<RPCNodeHashing> {

	final static String DEFAULT_SCHEMA = "tcp";
	final static Set<String> purgeSchema = new HashSet<String>(Arrays.asList("tcp", "udp"));
	protected ArrayList<RPCNode> nodes;
	protected ArrayList<String> urls;
	protected ArrayList<Double> decays;

	private RPCNodeHashing() {
	}

	public static String getRawConnURL(String url) throws IllegalArgumentException {
		if (url == null) {
			return null;
		}
		int pos = url.lastIndexOf("://");
		String ret = url;
		if (pos > 0) {
			pos += 3;
			ret = url.substring(pos);
		}
		int first = ret.indexOf(':');
		if (first < 0 || first != ret.lastIndexOf(':')) {
			throw new IllegalArgumentException("not valid rpc connection string: " + url);
		}
		return ret;
	}

	public static String getRawConnSchema(String url) throws IllegalArgumentException {
		if (url == null) {
			return null;
		}
		int pos = url.indexOf("://");
		if (pos < 0) {
			return DEFAULT_SCHEMA;
		}
		return url.substring(0, pos);
	}

	public static String getSafeConnURL(String url) throws IllegalArgumentException {
		String ret = getRawConnURL(url);
		if (ret != null) {
			ret = ret.replace(':', '_');
		}
		return ret;
	}

	@Override
	public int compareTo(RPCNodeHashing o) {
		// 1. size
		if (this.nodes.size() != o.nodes.size()) {
			return this.nodes.size() - o.nodes.size();
		}
		// 2. items
		for (int i = 0; i < nodes.size(); i++) {
			if (!nodes.get(i).equals(o.nodes.get(i))) {
				return nodes.get(i).compareTo(o.nodes.get(i));
			}
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RPCNodeHashing)) {
			return false;
		}
		RPCNodeHashing u = (RPCNodeHashing) obj;
		return this.toString().equals(u.toString());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public String toString() {
		return "" + nodes;
	}

	public List<RPCNode> getNodes() {
		return nodes;
	}

	public List<String> getURLs() {
		return urls;
	}

	public List<Double> getDecays() {
		return decays;
	}

	static void sortNodes(RPCNodeHashing h) {
		Collections.sort(h.nodes);
		ArrayList<String> idx = new ArrayList<String>();
		for (RPCNode n : h.nodes) {
			idx.add(n.url);
		}
		h.urls = idx;
		calcDecay(h, RPCNode.HEARTBEAT_PERIOD);
	}

	static void calcDecay(RPCNodeHashing h, double period) {
		ArrayList<Double> de = new ArrayList<Double>();
		boolean haltit = false;
		Double d = null;
		long hb = 0;
		for (RPCNode n : h.nodes) {
			if (n.halted == null) { // if online
				haltit = false;
			} else {
				haltit = (System.currentTimeMillis() - n.halted) >= 0;
			}
			d = null; // offline
			if (!haltit) { // heartbeat check
				hb = n.heartbeat == null ? System.currentTimeMillis() - 1 : n.heartbeat;
				d = (System.currentTimeMillis() - hb) * 1.0d / period;
				if (d >= RPCNode.MAX_LOSE_CONTACT) {
					d = null;
				} else {
					d = d <= 1 ? 1 : d; // decay using t
				}
			}
			de.add(d);
		}
		h.decays = de;
	}

	static public RPCNodeHashing createFromString(List<String> urls) throws IllegalArgumentException {
		if (urls == null || urls.isEmpty()) {
			throw new IllegalArgumentException("empty rpc urls! ");
		}
		RPCNodeHashing ret = new RPCNodeHashing();
		ret.nodes = new ArrayList<>();
		RPCNode node;
		Set<String> sch = new HashSet<String>();
		for (String u : urls) {
			node = new RPCNode();
			node.protocol = getRawConnSchema(u);
			node.url = getRawConnURL(u);
			node.group = null;
			node.up = new Date().getTime();
			node.heartbeat = new Date().getTime();
			node.load = 1.0d;
			ret.nodes.add(node);
			sch.add(node.protocol);
		}
		if (!checkSchema(sch)) {
			throw new IllegalArgumentException("difference schema in rpc urls: " + urls);
		}
		sortNodes(ret);
		return ret;
	}

	static public RPCNodeHashing createFromNodes(List<RPCNode> nodes) throws IllegalArgumentException {
		if (nodes == null || nodes.isEmpty()) {
			throw new IllegalArgumentException("empty rpc urls! ");
		}
		RPCNodeHashing ret = new RPCNodeHashing();
		ret.nodes = new ArrayList<>();
		Set<String> sch = new HashSet<String>();
		for (RPCNode node : nodes) {
			ret.nodes.add(node);
			if (node.protocol == null) {
				node.protocol = getRawConnSchema(node.url);
			}
			sch.add(node.protocol);
		}
		if (!checkSchema(sch)) {
			throw new IllegalArgumentException("difference schema in rpc urls: " + nodes);
		}
		sortNodes(ret);
		return ret;
	}

	static public RPCNodeHashing createFromHashMap(LinkedHashMap<String, Double> nodes)
			throws IllegalArgumentException {
		if (nodes == null || nodes.isEmpty()) {
			throw new IllegalArgumentException("empty rpc urls! ");
		}
		RPCNodeHashing ret = new RPCNodeHashing();
		ret.nodes = new ArrayList<RPCNode>();
		RPCNode node;
		Set<String> sch = new HashSet<String>();
		for (Entry<String, Double> en : nodes.entrySet()) {
			node = new RPCNode();
			node.protocol = getRawConnSchema(en.getKey());
			node.url = getRawConnURL(en.getKey());
			node.group = null;
			node.up = new Date().getTime();
			node.heartbeat = new Date().getTime();
			node.load = en.getValue();
			ret.nodes.add(node);
			sch.add(node.protocol);
		}
		if (!checkSchema(sch)) {
			throw new IllegalArgumentException("difference schema in rpc urls: " + nodes);
		}
		sortNodes(ret);
		return ret;
	}

	static boolean checkSchema(Set<String> sch) {
		if (sch.size() < 2) {
			return true;
		} else if (sch.size() == purgeSchema.size()) {
			int c = 0;
			for (String s : sch) {
				if (purgeSchema.contains(StringUtils.lowerCase(s))) {
					c++;
				}
			}
			if (c == purgeSchema.size()) {
				return true;
			}
		}
		return false;
	}

	public LinkedHashMap<String, Double> reverseToMap() {
		LinkedHashMap<String, Double> ret = new LinkedHashMap<>();
		RPCNode n;
		for (int i = 0; i < nodes.size(); i++) {
			n = nodes.get(i);
			ret.put(n.url, n.getSafeLoad(1.0d));
		}
		return ret;
	}

}
