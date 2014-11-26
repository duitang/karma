package com.duitang.service.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.avro.Protocol;
import org.apache.avro.ipc.CallFuture;
import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.Transceiver;

public class MinaTransceiver extends Transceiver {

	protected Headquarter headquarter;
	protected Callback<List<ByteBuffer>> cb;

	public MinaTransceiver(InetSocketAddress addr) throws IOException {
		headquarter = Headquarter.getHeadquarter(addr.getHostName(), addr.getPort());
	}

	@Override
	public boolean isConnected() {
		return headquarter.getRemote() != null;
	}

	@Override
	public Protocol getRemote() {
		return headquarter.getRemote();
	}

	@Override
	public void setRemote(Protocol protocol) {
		headquarter.setRemote(protocol);
	}

	@Override
	public String getRemoteName() throws IOException {
		return headquarter.getRemoteName();
	}

	@Override
	public List<ByteBuffer> readBuffers() throws IOException {
		System.out.println(cb);
		return null;
	}

	@Override
	public void writeBuffers(List<ByteBuffer> buffers) throws IOException {
		headquarter.request(buffers, cb);
	}

	@Override
	public List<ByteBuffer> transceive(List<ByteBuffer> request) throws IOException {
		try {
			CallFuture<List<ByteBuffer>> transceiverFuture = new CallFuture<List<ByteBuffer>>();
			transceive(request, transceiverFuture);
			return transceiverFuture.get();
		} catch (InterruptedException e) {
			return null;
		} catch (ExecutionException e) {
			return null;
		}
	}

	@Override
	public void transceive(List<ByteBuffer> request, Callback<List<ByteBuffer>> callback) throws IOException {
		cb = callback;
		writeBuffers(request);
	}
}
