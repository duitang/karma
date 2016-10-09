/**
 * @author laurence
 * @since 2016年10月9日
 *
 */
package com.duitang.service.karma.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author laurence
 * @since 2016年10月9日
 *
 */
public class RPCUrls implements Comparable<RPCUrls> {

	final static protected String DEFAULT_SCHEMA = "tcp";
	final protected List<String> nodes;
	final protected String schema;

	public RPCUrls(String url) throws IllegalArgumentException {
		this(Arrays.asList(url));
	}

	public RPCUrls(LinkedHashMap<String, Double> urls) throws IllegalArgumentException {
		List<String> lst = new ArrayList<String>();
		for (Entry<String, Double> en : urls.entrySet()) {
			lst.add(en.getKey());
		}
		nodes = initNodes(lst);
		schema = initSchema(lst);
	}

	public RPCUrls(List<String> urls) throws IllegalArgumentException {
		nodes = initNodes(urls);
		schema = initSchema(urls);
	}

	protected List<String> initNodes(List<String> src) throws IllegalArgumentException {
		List<String> ret = new ArrayList<String>();
		Set<String> st = new HashSet<String>(src);
		for (String u : st) {
			ret.add(getRawConnURL(u));
		}
		Collections.sort(ret);
		return ret;
	}

	protected String initSchema(List<String> src) throws IllegalArgumentException {
		// you are fucking passing non-empty list
		String ret = getRawConnSchema(src.get(0));
		for (String s : src) {
			if (!ret.equals(getRawConnSchema(s))) {
				throw new IllegalArgumentException("different schema in list: " + src);
			}
		}
		return ret;
	}

	public static String getRawConnURL(String url) throws IllegalArgumentException {
		if (url == null) {
			return null;
		}
		int pos = url.indexOf("://");
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

	public static LinkedHashMap<String, Double> getRawConnURL(LinkedHashMap<String, Double> src) {
		LinkedHashMap<String, Double> ret = new LinkedHashMap<String, Double>();
		for (Entry<String, Double> en : src.entrySet()) {
			ret.put(getRawConnURL(en.getKey()), en.getValue());
		}
		return ret;
	}

	@Override
	public int compareTo(RPCUrls o) {
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
		if (obj instanceof RPCUrls) {
			RPCUrls u = (RPCUrls) obj;
			boolean bnodes = this.nodes.equals(u.nodes);
			boolean bschema = this.schema.equals(u.schema);
			return bnodes && bschema;
		}
		return false;
	}

	public List<String> getNodes() {
		return nodes;
	}

	public String getSchema() {
		return schema;
	}

}
