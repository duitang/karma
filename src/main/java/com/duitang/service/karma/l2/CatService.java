package com.duitang.service.karma.l2;

import java.nio.ByteBuffer;
import java.util.Map;

public interface CatService {

	boolean cat_setstring(String key, String value, int ttl);

	boolean cat_addstring(String key, String value, int ttl);

	long cat_incr(String key, long delta);

	String cat_getstring(String key);

	boolean cat_delstring(String key);

	Map<String, String> cat_mgetstring(String keys);

	ByteBuffer cat_getbytes(String key);

	boolean cat_setbytes(String key, ByteBuffer value, int ttl);

}
