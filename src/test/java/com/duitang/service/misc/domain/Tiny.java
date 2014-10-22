package com.duitang.service.misc.domain;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.avro.reflect.AvroEncode;

import com.duitang.service.codecs.Map2Json;
import com.duitang.service.codecs.Set2Json;

public class Tiny {

	// @AvroSchema("{\"type\":\"map\",\"values\":[\"string\",\"null\",\"int\"],\"default\":null}")
	@AvroEncode(using = Map2Json.class)
	protected HashMap data;

	@AvroEncode(using = Set2Json.class)
	protected HashSet ss;

	public HashMap getData() {
		return data;
	}

	public void setData(HashMap data) {
		this.data = data;
	}

	public HashSet getSs() {
		return ss;
	}

	public void setSs(HashSet ss) {
		this.ss = ss;
	}

}
