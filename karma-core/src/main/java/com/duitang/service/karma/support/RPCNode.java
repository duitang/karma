/**
 * @author laurence
 * @since 2016年10月10日
 *
 */
package com.duitang.service.karma.support;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author laurence
 * @since 2016年10月10日
 *
 */
public class RPCNode implements Comparable<RPCNode> {

	static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}

	public static long HEART_BEAT_PERIOD = 10 * 1000; // 30s
	public static long MAX_LOSE_CONTACT = 3; // 3 period

	public String url;
	public String protocol;
	public String group;
	public Boolean online;
	public Long up; // up time
	public String upcaption; // just for production human traceable information
	public Long heartbeat;
	public String hbcaption; // just for production human traceable information
	public Double load; // currently not used
	public Long halted; // halted time
	public String htcaption; // just for production human traceable information

	public String toDataString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (up != null) {
			upcaption = sdf.format(up);
		}
		if (heartbeat != null) {
			hbcaption = sdf.format(heartbeat);
		}
		if (halted != null) {
			htcaption = sdf.format(halted);
		}
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static RPCNode fromBytes(byte[] src) {
		try {
			return mapper.readValue(src, RPCNode.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public double getSafeLoad(double def) {
		return load == null ? def : load;
	}

	@JsonIgnore
	public boolean isAlive() {
		boolean alive = halted == null ? true : ((System.currentTimeMillis() - halted) < 0);
		return url != null && alive;
	}

	public boolean diff(RPCNode node) {
		if (node == null) {
			return true;
		}

		boolean ur = !StringUtils.equals(node.url, this.url);
		boolean p = !StringUtils.equals(node.protocol, this.protocol);
		boolean g = !StringUtils.equals(node.group, this.group);
		boolean o = node.online != this.online;
		boolean u = node.up != this.up;
		return ur || p || g || o || u;
	}

	public String toString() {
		return protocol + "://" + url;
	}

	@Override
	public int compareTo(RPCNode o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RPCNode) {
			return !diff((RPCNode) obj);
		}
		return false;
	}

}
