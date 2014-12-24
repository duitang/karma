package com.duitang.service.demo;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

public interface DemoService {

	String memory_getString(String key);

	boolean memory_setString(String key, String value, int ttl);

	ByteBuffer memory_getBytes(String key);

	boolean memory_setBytes(String key, ByteBuffer value, int ttl);

	String trace_msg(String key, long ttl);

	Map getmap(String name);

	boolean setmap(String name, Map data);

	Set<String> noparam();

	Map getM(Set s);

	void getError();

}