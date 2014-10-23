package com.duitang.service.data;

import java.io.Serializable;
import java.util.Map;

import org.apache.avro.reflect.AvroEncode;

import com.duitang.service.codecs.Map2Json;

public class MapData implements Serializable {

	private static final long serialVersionUID = 1L;

	@AvroEncode(using = Map2Json.class)
	protected Map data;

	public MapData() {
	}

	public MapData(Map d) {
		this.data = d;
	}

	public Map getData() {
		return data;
	}

	public void setData(Map data) {
		this.data = data;
	}

}
