package com.duitang.service.demo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.AvroRemoteException;

import com.duitang.service.base.MetricCenter;

public class MemoryCacheService implements DemoService {

	protected Map<String, String> memory = new HashMap();
	protected Map<String, ByteBuffer> memoryB = new HashMap();

	protected String clientid = null;

	public MemoryCacheService() {
		initClientName();
		MetricCenter.initMetric(DemoService.class, clientid);
	}

	protected void initClientName() {
		if (clientid == null) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			StackTraceElement e = stacktrace[2];
			if (e.getMethodName() != null) {
				clientid = e.getFileName() + "@" + e.getLineNumber() + ":" + e.getMethodName();
			}
		}
		if (clientid == null) {
			clientid = "";
		}
	}

	@Override
	public String memory_getString(String key) throws AvroRemoteException {
		long ts = System.currentTimeMillis();
		String ret = memory.get(key);
		MetricCenter.methodMetric(clientid + ":memory_getString", ts);
		return ret;
	}

	@Override
	public boolean memory_setString(String key, String value, int ttl) throws AvroRemoteException {
		long ts = System.currentTimeMillis();
		memory.put(key, value); // mock, so ignore ttl
		MetricCenter.methodMetric(clientid + ":memory_setString", ts);
		return true;
	}

	@Override
	public ByteBuffer memory_getBytes(String key) throws AvroRemoteException {
		long ts = System.currentTimeMillis();
		ByteBuffer ret = memoryB.get(key);
		MetricCenter.methodMetric(clientid + ":memory_getBytes", ts);
		return ret;
	}

	@Override
	public boolean memory_setBytes(String key, ByteBuffer value, int ttl) throws AvroRemoteException {
		long ts = System.currentTimeMillis();
		memoryB.put(key, value);
		MetricCenter.methodMetric(clientid + ":memory_setBytes", ts);
		return true;
	}

	@Override
	public String trace_msg(String key, long ttl) throws AvroRemoteException {
		long ts = System.currentTimeMillis();
		try {
			Thread.sleep(ttl);
		} catch (InterruptedException e) {
		}
		long ela = System.currentTimeMillis() - ts;
		String ret = key + " => {" + ts + ", " + ela + "}";
		MetricCenter.methodMetric(clientid + ":trace_msg", ts);
		return ret;
	}

}
