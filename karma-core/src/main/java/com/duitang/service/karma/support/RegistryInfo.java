/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
package com.duitang.service.karma.support;

import java.util.ArrayList;
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

	public RegistryInfo(boolean freezing, LinkedHashMap<String, Float> n) {
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

	static public RegistryInfo purged(boolean freezing, RPCNodeHashing hashing) {
		List<RPCNode> nodes = new ArrayList<>();
		for (int i = 0; i < hashing.getNodes().size(); i++) {
			if (hashing.decays.get(i) != null) {
				nodes.add(hashing.getNodes().get(i));
			}
		}
		if (nodes.isEmpty()) { // at least 1 node alive
			nodes.add(hashing.getNodes().get(0));
		}
		return new RegistryInfo(freezing, RPCNodeHashing.createFromNodes(nodes));
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
			s = hashing.getNodes().toString() + "; " + hashing.decays;
		}
		return "freezing:" + freezeMode + "; " + s;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RegistryInfo)) {
			return false;
		}
		RegistryInfo o = ((RegistryInfo) obj);
		return this.toString().equals(o.toString());
	}

	public RPCNodeHashing calcDecayFactor(RPCNodeHashing snap) {
		// 1. merge from snap if node is not exist at current and not halted
		List<RPCNode> extra = new ArrayList<RPCNode>();
		extra.addAll(this.hashing.getNodes());
		for (int i = 0; i < snap.getNodes().size(); i++) {
			RPCNode node = snap.getNodes().get(i);
			int idx = this.hashing.getURLs().indexOf(node.url);
			if (idx < 0) {
				extra.add(node);
			}
		}
		RPCNodeHashing ret = RPCNodeHashing.createFromNodes(extra);
		RPCNodeHashing.calcDecay(ret, RPCNode.HEARTBEAT_PERIOD);
		return ret;
	}

}
