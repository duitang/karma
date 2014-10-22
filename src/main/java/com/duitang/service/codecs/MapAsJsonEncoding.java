package com.duitang.service.codecs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.reflect.CustomEncoding;
import org.codehaus.jackson.map.ObjectMapper;

public class MapAsJsonEncoding extends CustomEncoding<Map> {
	{
		schema = Schema.create(Schema.Type.STRING);
		schema.addProp("CustomEncoding", "MapAsJsonEncoding");
	}

	protected static ObjectMapper mapper = new ObjectMapper();

	@Override
	protected final void write(Object datum, Encoder out) throws IOException {
		out.writeString(mapper.writeValueAsString(datum));
	}

	@Override
	protected final Map read(Object reuse, Decoder in) throws IOException {
		if (reuse != null && reuse instanceof String) {
			return (Map) mapper.readValue((String) reuse, HashMap.class);
		} else
			return (Map) mapper.readValue(in.readString(), HashMap.class);
	}
	
}
