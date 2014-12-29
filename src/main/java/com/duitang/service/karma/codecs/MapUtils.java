package com.duitang.service.karma.codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.codehaus.jackson.map.ObjectMapper;

public class MapUtils {

	static protected ObjectMapper mapper = new ObjectMapper();

	public static ByteBuffer objectToBytes(Object src) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(src);
			oos.flush();
			ByteBuffer ret = ByteBuffer.wrap(bos.toByteArray());
			ret.position(bos.size()).flip();
			return ret;
		} catch (IOException e) {
		}
		return null;
	}

	public static <T> T bytesToObject(ByteBuffer src, Class<T> type) {
		ByteArrayInputStream bis = new ByteArrayInputStream(src.array());
		try {
			ObjectInputStream ois = new ObjectInputStream(bis);
			return (T) ois.readObject();
		} catch (Exception e) {
		}
		return null;
	}

	public static String objectToJson(Object src) {
		try {
			return mapper.writeValueAsString(src);
		} catch (Exception e) {
		}
		return "";
	}

	public static <T> T jsonToObject(String src, Class<T> type) {
		try {
			return mapper.readValue(src, type);
		} catch (Exception e) {
		}
		return null;
	}

}
