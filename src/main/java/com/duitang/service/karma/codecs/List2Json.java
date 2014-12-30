package com.duitang.service.karma.codecs;

import java.util.HashMap;
import java.util.Map;

public class List2Json extends JsonEncoding<Map> {

	@Override
	protected Class getResultType() {
		return HashMap.class;
	}

}