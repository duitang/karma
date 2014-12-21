package com.duitang.service.demo;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

import com.duitang.service.data.MapData;

public interface DemoService {

	String memory_getString(String key);

	boolean memory_setString(String key, String value, int ttl);

	ByteBuffer memory_getBytes(String key);

	boolean memory_setBytes(String key, ByteBuffer value, int ttl);

	String trace_msg(String key, long ttl);

	MapData getmap(String name);

	boolean setmap(String name, MapData data);

	Set<String> noparam();

	Map getM(Set s);

}