package com.duitang.service.karma.codecs;

import java.util.HashMap;
import java.util.Map;

public class Map2Json extends JsonEncoding<Map> {

	@Override
	protected Class getResultType() {
		return HashMap.class;
	}

}