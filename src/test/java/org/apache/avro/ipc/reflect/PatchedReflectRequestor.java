package org.apache.avro.ipc.reflect;

import java.io.IOException;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.reflect.ReflectData;

public class PatchedReflectRequestor extends ReflectRequestor {

	public PatchedReflectRequestor(Class<?> iface, Transceiver transceiver, ReflectData data) throws IOException {
		super(iface, transceiver, data);
		// TODO Auto-generated constructor stub
	}

	public PatchedReflectRequestor(Class<?> iface, Transceiver transceiver) throws IOException {
		super(iface, transceiver);
		// TODO Auto-generated constructor stub
	}

	public PatchedReflectRequestor(Protocol protocol, Transceiver transceiver, ReflectData data) throws IOException {
		super(protocol, transceiver, data);
		// TODO Auto-generated constructor stub
	}

	public PatchedReflectRequestor(Protocol protocol, Transceiver transceiver) throws IOException {
		super(protocol, transceiver);
		// TODO Auto-generated constructor stub
	}

	
}
