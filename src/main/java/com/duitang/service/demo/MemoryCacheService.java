package com.duitang.service.demo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.AvroRemoteException;

public class MemoryCacheService implements DemoService {

	protected Map<String, String> memory = new HashMap();
	protected Map<String, ByteBuffer> memoryB = new HashMap();

	@Override
	public String memory_getString(String key) throws AvroRemoteException {
		return memory.get(key);
	}

	@Override
	public boolean memory_setString(String key, String value, int ttl) throws AvroRemoteException {
		memory.put(key, value); // mock, so ignore ttl
		return true;
	}

	@Override
	public ByteBuffer memory_getBytes(String key) throws AvroRemoteException {
		return memoryB.get(key);
	}

	@Override
	public boolean memory_setBytes(String key, ByteBuffer value, int ttl) throws AvroRemoteException {
		memoryB.put(key, value);
		return true;
	}

}
