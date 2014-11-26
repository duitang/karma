package org.apache.avro.ipc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.Requestor.Request;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.reflect.ReflectData;

import com.duitang.service.demo.DemoService;

public class PackageTester {

	static public void main(String[] args) throws Exception {
		Object param = new Object[] { "aaa", "bbb", 50000 };
		// Dummy requestor = new Dummy(DemoService.class, new
		// MinaTransceiver(new InetSocketAddress("localhost", 9999)));
		//
		// Request req = (Request) requestor.request("memory_setString", param);
		// System.out.println(req.getBytes());

		List<ByteBuffer> ret = PackageTester.mock(DemoService.class, "memory_setString", param);
		System.out.println(ret);
	}

	static public List<ByteBuffer> mock(Class iface, String methodName, Object param) throws Exception {
		Dummy requestor = new Dummy(iface, new Transceiver() {

			@Override
			public String getRemoteName() throws IOException {
				return new InetSocketAddress("localhost", 9999).toString();
			}

			@Override
			public List<ByteBuffer> readBuffers() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void writeBuffers(List<ByteBuffer> buffers) throws IOException {
				// TODO Auto-generated method stub

			}

		});
		Request req = (Request) requestor.request("memory_setString", param);
		return req.getBytes();
	}
}

class Dummy extends ReflectRequestor {

	public Dummy(Class<?> iface, Transceiver transceiver, ReflectData data) throws IOException {
		super(iface, transceiver, data);
		// TODO Auto-generated constructor stub
	}

	public Dummy(Class<?> iface, Transceiver transceiver) throws IOException {
		super(iface, transceiver);
		// TODO Auto-generated constructor stub
	}

	public Dummy(Protocol protocol, Transceiver transceiver, ReflectData data) throws IOException {
		super(protocol, transceiver, data);
		// TODO Auto-generated constructor stub
	}

	public Dummy(Protocol protocol, Transceiver transceiver) throws IOException {
		super(protocol, transceiver);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object request(String messageName, Object request) throws Exception {
		Request ret = new Request(messageName, request, new RPCContext());
		return ret;
	}

}
