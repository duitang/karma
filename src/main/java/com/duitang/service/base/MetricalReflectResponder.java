package com.duitang.service.base;

import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.apache.avro.reflect.ReflectData;

public class MetricalReflectResponder extends ReflectResponder {

	protected String clientid;

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public MetricalReflectResponder(Class iface, Object impl, ReflectData data) {
		super(iface, impl, data);
	}

	public MetricalReflectResponder(Class iface, Object impl) {
		super(iface, impl);
	}

	public MetricalReflectResponder(Protocol protocol, Object impl, ReflectData data) {
		super(protocol, impl, data);
	}

	public MetricalReflectResponder(Protocol protocol, Object impl) {
		super(protocol, impl);
	}

	@Override
	public Object respond(Message message, Object request) throws Exception {
		long ts = System.currentTimeMillis();
		boolean fail = false;
		try {
			return super.respond(message, request);
		} catch (Exception e) {
			fail = true;
			throw e;
		} finally {
			ts = System.currentTimeMillis() - ts;
			MetricCenter.methodMetric(clientid, message.getName(), ts, fail);
		}
	}

}
