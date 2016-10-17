/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
package com.duitang.service.karma.support;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
public class RegistryInfo {

	protected RPCNodeHashing hashing;
	protected boolean freezeMode;

	public RegistryInfo(boolean freezing, LinkedHashMap<String, Double> n) {
		this.hashing = RPCNodeHashing.createFromHashMap(n);
		this.freezeMode = freezing;
	}

	public RegistryInfo(boolean freezing, RPCNodeHashing url) {
		this.freezeMode = freezing;
		this.hashing = url;
	}

	public RegistryInfo(boolean freezing, List<RPCNode> nodes) {
		this.freezeMode = freezing;
		this.hashing = RPCNodeHashing.createFromNodes(nodes);
	}

	public RPCNode getNode(String url) {
		String u = RPCNodeHashing.getRawConnURL(url);
		int idx = hashing.urls.indexOf(u);
		RPCNode ret = null;
		if (idx >= 0) {
			ret = hashing.nodes.get(idx);
		}
		return ret;
	}

	public List<RPCNode> getNodes() {
		return hashing.getNodes();
	}

	public RPCNodeHashing getHashing() {
		return hashing;
	}

	public List<String> getURLs() {
		return hashing.getURLs();
	}

	public boolean isFreezeMode() {
		return freezeMode;
	}

	public String toString() {
		String s = "[]";
		if (hashing != null && hashing.getNodes() != null) {
			s = hashing.getNodes().toString();
		}
		return "freezing:" + freezeMode + "; " + s;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RegistryInfo){
			return this.toString().equals(obj.toString());
		}
		return super.equals(obj);
	}

}
