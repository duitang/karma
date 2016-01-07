package com.duitang.service.karma.demo;

import java.util.Map;
import java.util.Set;

public interface IDemoService {

	String memory_getString(String key);

	boolean memory_setString(String key, String value, int ttl);

	byte[] memory_getBytes(String key);

	boolean memory_setBytes(String key, byte[] value, int ttl);

	String trace_msg(String key, long ttl);

	Map getmap(String name);

	boolean setmap(String name, Map data);

	Set<String> noparam();

	Map getM(Set s);

	void getError();

}