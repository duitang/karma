package com.duitang.service.demo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TException;

public class MemoryCacheService implements MemoryCache.Iface {

	protected Map<String, String> memory = new HashMap();
	protected Map<String, ByteBuffer> memoryB = new HashMap();

	@Override
	public String getString(String key) throws TException {
		return memory.get(key);
	}

	@Override
	public void setString(String key, String value, int ttl) throws TException {
		memory.put(key, value); // mock, so ignore ttl
	}

	@Override
	public void setBytes(String key, ByteBuffer value, int ttl) throws TException {
		memoryB.put(key, value);
	}

	@Override
	public ByteBuffer getBytes(String key) throws TException {
		return memoryB.get(key);
	}

}
