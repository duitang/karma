package com.duitang.service.karma.trace;

import java.io.IOException;

import com.duitang.service.karma.boot.KarmaServerConfig;

/**
 * <pre>
 * 相对于TracePoint重量级的，非常方便的自动追踪工具类，切记必须释放
 * make sure close it, or resource leaks
 * </pre>
 * 
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class TraceStone implements AutoCloseable {

	protected TraceBlock tc;

	public TraceStone() {
		String[] names = getNames();
		init(names[0], names[1]);
	}

	public TraceStone(String claz, String name) {
		init(claz, name);
	}

	protected String[] getNames() {
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[3];
		String methodName = e.getMethodName();
		return new String[] { e.getClassName(), methodName };
	}

	protected void init(String claz, String name) {
		Long[] ids = TraceContextHolder.snap();
		tc = new TraceBlock(true, null, null);
		TraceContextHolder.push(tc);
		tc.setIds(ids[0], ids[1]);
		tc.clazzName = claz;
		tc.name = name;
		tc.isLocal = true;
		tc.active();
	}

	@Override
	public void close() throws IOException {
		close(null);
	}

	public void close(Throwable e) throws IOException {
		tc.passivate(e);
		TraceContextHolder.release();
		KarmaServerConfig.tracer.visit(tc);
	}

	public void setInfo(String host, Integer port, Long pid, String group) {
		tc.host = host;
		tc.port = port;
		tc.pid = pid;
		tc.group = group;
	}

	public String getAttribute(String key) {
		return tc.props.get(key);
	}

	public void setAttribute(String key, String val) {
		tc.props.put(key, val);
	}

}
