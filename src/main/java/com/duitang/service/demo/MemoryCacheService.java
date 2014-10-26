package com.duitang.service.demo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.duitang.service.data.MapData;

public class MemoryCacheService implements DemoService, DemoServiceSpec {

	protected Map<String, String> memory = new HashMap();
	protected Map<String, ByteBuffer> memoryB = new HashMap();
	protected Map<String, MapData> mapA = new HashMap();

	@Override
	public String memory_getString(String key) {
		return memory.get(key);
	}

	@Override
	public boolean memory_setString(String key, String value, int ttl) {
		memory.put(key, value); // mock, so ignore ttl
		return true;
	}

	@Override
	public ByteBuffer memory_getBytes(String key) {
		return memoryB.get(key);
	}

	@Override
	public boolean memory_setBytes(String key, ByteBuffer value, int ttl) {
		memoryB.put(key, value);
		return true;
	}

	@Override
	public String trace_msg(String key, long ttl) {
		long ts = System.currentTimeMillis();
		try {
			Thread.sleep(ttl);
		} catch (InterruptedException e) {
		}
		long ela = System.currentTimeMillis() - ts;
		String ret = key + " => {" + ts + ", " + ela + "}";
		return ret;
	}

	@Override
	public MapData getmap(String name) {
		return mapA.get(name);
	}

	@Override
	public boolean setmap(String name, MapData data) {
		mapA.put(name, data);
		return true;
	}

}
