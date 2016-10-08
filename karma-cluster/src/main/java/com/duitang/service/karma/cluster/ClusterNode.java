/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
package com.duitang.service.karma.cluster;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;

import com.duitang.service.karma.client.RegistryInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author laurence
 * @since 2016年10月4日
 *
 */
public class ClusterNode {

	static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}
	static final String zkBase = "/karma_rpc";
	static final String zkNodeBase = zkBase + "/nodes";

	static final long HEART_BEAT_PERIOD = 30 * 1000; // 30s
	static final long MAX_LOSE_CONTACT = 5; // 5 period

	public String url;
	public String protocol;
	public String group;
	public Boolean online;
	public Long up; // up time
	public String upcaption; // just for production human traceable information
	public Long heartbeat;
	public String hbcaption; // just for production human traceable information
	public Integer load; // currently not used

	public String toDataString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (up != null) {
			upcaption = sdf.format(up);
		}
		if (heartbeat != null) {
			hbcaption = sdf.format(heartbeat);
		}
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static ClusterNode fromBytes(byte[] src) {
		try {
			return mapper.readValue(src, ClusterNode.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@JsonIgnore
	public boolean isAlive() {
		boolean alive = (System.currentTimeMillis() - heartbeat) < MAX_LOSE_CONTACT * HEART_BEAT_PERIOD;
		return url != null && alive;
	}

	public boolean diff(ClusterNode node) {
		if (node == null) {
			return true;
		}
		boolean p = !node.protocol.equals(this.protocol);
		boolean g = !node.group.equals(this.group);
		boolean o = node.online != this.online;
		boolean u = node.up != this.up;
		return p || g || o || u;
	}

	public static RegistryInfo toTinyMap(List<ClusterNode> nodes) {
		LinkedHashMap<String, Double> r = new LinkedHashMap<>();
		for (ClusterNode cn : nodes) {
			// current always using 1.0
			r.put(RegistryInfo.getConnectionURL(cn.url), 1.0d);
		}
		RegistryInfo ret = new RegistryInfo();
		ret.wNodes = r;
		return ret;
	}

}
