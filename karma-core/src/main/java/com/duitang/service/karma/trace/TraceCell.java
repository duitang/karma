/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
package com.duitang.service.karma.trace;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.duitang.service.karma.meta.RPCConfig;

/**
 * @author laurence
 * @since 2016年9月25日
 *
 */
public class TraceCell {

	public final static String TRACE_ID = "traceid";
	public final static String SPAN_ID = "spanid";
	public final static String SAMPLED = "sampled";

	final static String[][] types = new String[][] { { "cs", "cr" }, { "sr", "ss" } };
	final static protected Random rnd = new Random();

	// initialization
	public long timestamp; // happen at
	public boolean sampled; // if sampled
	public long traceId; // not null
	public long spanId; // not null
	public Long parentId; // maybe null
	public String clazzName; // which class name
	public String name; // name
	public String[] type; // {c,s} => cs, cr, ss, sr, lr, ls
	// ...

	public long ts1; // begin time
	public long ts2; // end time

	public String host; // hostname
	public Integer port; // service port
	public Long pid; // PID
	public boolean isLocal = false; // is local call

	public String group; // group name
	public long duration; // lasting
	public boolean successful; // top layer
	public String err; // exception

	public TraceCell(boolean client, String host, Integer port) {
		this.timestamp = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
		this.type = client ? types[0] : types[1];
		this.host = host;
		this.port = port;
	}

	public TraceCell(
			boolean client,
			String host,
			Integer port,
			Long traceId,
			Long spanId,
			Boolean sampled,
			String clazName,
			String name,
			String group) {
		this.timestamp = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
		this.type = client ? types[0] : types[1];
		this.sampled = sampled == null ? false : sampled;
		setIds(traceId, spanId);
		this.clazzName = clazName;
		this.name = name;
		this.group = group;
	}

	public TraceCell(
			boolean client,
			String host,
			Integer port,
			RPCConfig cfg,
			String clazName,
			String name,
			String group) {
		this(client, host, port);
		fillInfo(cfg, clazName, name, group);
	}

	public void setIds(Long traceId, Long spanId) {
		this.parentId = spanId;
		this.traceId = traceId == null ? rnd.nextLong() : traceId;
		this.spanId = this.parentId == null ? this.traceId : rnd.nextLong();
	}

	public void fillInfo(RPCConfig cfg, String clazName, String name, String group) {
		if (cfg != null) {
			Long traceId = (Long) cfg.getConf(TRACE_ID);
			Long spanId = (Long) cfg.getConf(SPAN_ID);
			Boolean sampled = (Boolean) cfg.getConf(SAMPLED);
			this.sampled = sampled == null ? false : sampled;
			setIds(traceId, spanId);
		}
		this.clazzName = clazName;
		this.name = name;
		this.group = group;
	}

	public void active() {
		this.ts1 = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
	}

	public void passivate(Throwable t) {
		String err = null;
		if (t != null) {
			err = ExceptionUtils.getMessage(t);
		}
		passivate(err);
	}

	public void passivate(String err) {
		this.ts2 = TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis());
		this.duration = this.ts2 - this.timestamp;
		this.successful = err == null ? true : false;
		this.err = err;
	}

	public boolean isPropagated() {
		return this.parentId != null;
	}

	@Override
	public String toString() {
		return "TraceCell{" + "timestamp=" + timestamp + ", sampled=" + sampled + ", traceId=" + traceId + ", spanId="
				+ spanId + ", parentId=" + parentId + ", clazzName='" + clazzName + '\'' + ", name='" + name + '\''
				+ ", type=" + Arrays.toString(type) + ", ts1=" + ts1 + ", ts2=" + ts2 + ", host='" + host + '\''
				+ ", port=" + port + ", pid=" + pid + ", isLocal=" + isLocal + ", group='" + group + '\''
				+ ", duration=" + duration + ", successful=" + successful + ", err='" + err + '\'' + '}';
	}
}
