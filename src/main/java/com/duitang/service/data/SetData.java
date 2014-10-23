package com.duitang.service.data;

import java.io.Serializable;
import java.util.Set;

import org.apache.avro.reflect.AvroEncode;

import com.duitang.service.codecs.Set2Json;

public class SetData implements Serializable {

	private static final long serialVersionUID = 1L;

	@AvroEncode(using = Set2Json.class)
	protected Set data;

	public SetData(Set d) {
		this.data = d;
	}

	public Set getData() {
		return data;
	}

	public void setData(Set data) {
		this.data = data;
	}

}
