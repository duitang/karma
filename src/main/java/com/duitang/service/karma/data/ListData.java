package com.duitang.service.karma.data;

import java.io.Serializable;
import java.util.List;

import org.apache.avro.reflect.AvroEncode;

import com.duitang.service.karma.codecs.List2Json;

public class ListData implements Serializable {

	private static final long serialVersionUID = 1L;

	@AvroEncode(using = List2Json.class)
	protected List data;

	public ListData() {
		super();
	}

	public ListData(List d) {
		this.data = d;
	}

	public List getData() {
		return data;
	}

	public void setData(List data) {
		this.data = data;
	}

}
