/**
 * @author laurence
 * @since 2016年10月26日
 *
 */
package com.duitang.service.karma.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * support class only for Node Debug Dumper
 * 
 * @author laurence
 * @since 2016年10月26日
 *
 */
public class NodeDD {

	final static ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}

	protected Map<String, String> attr = new HashMap<>();

	public void setAttr(String name, Object val) {
		attr.put(name, ArrayUtils.toString(val));
	}

	public Map getAtrrs() {
		return new HashMap(attr);
	}

	public String toString() {
		try {
			return mapper.writeValueAsString(this.attr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{}";
	}

}
