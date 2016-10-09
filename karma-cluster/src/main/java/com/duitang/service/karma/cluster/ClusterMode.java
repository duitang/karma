package com.duitang.service.karma.cluster;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterMode {

	static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}

	public LinkedHashMap<String, Double> nodes; // node connection string
	public Boolean freeze; // freeze mode if staging for deployment

	public String toDataString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static ClusterMode fromBytes(byte[] src) {
		try {
			return mapper.readValue(src, ClusterMode.class);
		} catch (IOException e) {
			// ignore
		}
		return null;
	}

}
