package com.duitang.service.karma.codecs;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.reflect.CustomEncoding;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class JsonEncoding<T> extends CustomEncoding<T> {
	{
		schema = Schema.create(Schema.Type.STRING);
		schema.addProp("CustomEncoding", this.getClass().getName() + "2JsonEncoding");
	}
	static ObjectMapper mapper = new ObjectMapper();

	@Override
	protected final void write(Object datum, Encoder out) throws IOException {
		out.writeString(mapper.writeValueAsString(datum));
	}

	@Override
	protected final T read(Object reuse, Decoder in) throws IOException {
		if (reuse != null && reuse instanceof String) {
			return (T) mapper.readValue((String) reuse, getResultType());
		} else
			return (T) mapper.readValue(in.readString(), getResultType());
	}

	protected abstract Class getResultType();

}
