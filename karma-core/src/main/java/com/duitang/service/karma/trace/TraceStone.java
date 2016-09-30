/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
package com.duitang.service.karma.trace;

import java.util.HashMap;
import java.util.Map;

import com.duitang.service.karma.meta.RPCConfig;

/**
 * <pre>
 * 比TraceCell重量级的追踪单元，可以加入任意的追踪元素，消耗相对较大，慎用
 * </pre>
 * 
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class TraceStone extends TraceCell {

	public Map<String, String> props = new HashMap<String, String>();

	public TraceStone(
			boolean client,
			String host,
			Integer port,
			Long traceId,
			Long spanId,
			Boolean sampled,
			String clazName,
			String name,
			String group) {
		super(client, host, port, traceId, spanId, sampled, clazName, name, group);
	}

	public TraceStone(
			boolean client,
			String host,
			Integer port,
			RPCConfig cfg,
			String clazName,
			String name,
			String group) {
		super(client, host, port, cfg, clazName, name, group);
	}

	public TraceStone(boolean client, String host, Integer port) {
		super(client, host, port);
	}

}
