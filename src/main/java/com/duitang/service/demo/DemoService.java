package com.duitang.service.demo;

import java.nio.ByteBuffer;

import com.duitang.service.data.MapData;

public interface DemoService {

	String memory_getString(String key);

	boolean memory_setString(String key, String value, int ttl);

	ByteBuffer memory_getBytes(String key);

	boolean memory_setBytes(String key, ByteBuffer value, int ttl);

	String trace_msg(String key, long ttl);

	MapData getmap(MapData data);

}