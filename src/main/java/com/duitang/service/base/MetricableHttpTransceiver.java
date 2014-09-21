package com.duitang.service.base;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.avro.ipc.Transceiver;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

public class MetricableHttpTransceiver extends Transceiver implements Closeable {

	static final String CONTENT_TYPE = "avro/binary";

	static final Collection<String> content_type = Arrays.asList(new String[] { CONTENT_TYPE });

	static protected int timeout = 500;
	static protected AsyncHttpClient client;

	protected URL url;
	protected Future<Response> resp;
	protected long startts = 0;
	protected long endts = -1;
	protected String clientid;

	static {
		initClient();
	}

	static public void setTimeout(int timeout) {
		MetricableHttpTransceiver.timeout = timeout;
		initClient();
	}

	static public void initClient() {
		client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeoutInMs(timeout).build());
	}

	public MetricableHttpTransceiver(String clientid, URL url) {
		this.clientid = clientid;
		this.url = url;
	}

	public String getRemoteName() {
		return this.url.toString();
	}

	public synchronized List<ByteBuffer> readBuffers() throws IOException {
		Response r;
		InputStream in = null;
		try {
			r = resp.get();
			in = r.getResponseBodyAsStream();
			return readBuffers(in);
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (resp == null) {
			return;
		}
		if (!resp.isDone()) {
			resp.cancel(true);
		}
	}

	public synchronized void writeBuffers(List<ByteBuffer> buffers) throws IOException {
		startts = System.currentTimeMillis();
		BoundRequestBuilder t = client.preparePost(getRemoteName());
		Map<String, Collection<String>> param = new HashMap<String, Collection<String>>();
		param.put("Content-Type", content_type);
		param.put("Content-Length", Arrays.asList(Integer.toString(getLength(buffers))));

		t.setParameters(param);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			writeBuffers(buffers, out);
			t.setBody(out.toByteArray());
			resp = t.execute();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	static int getLength(List<ByteBuffer> buffers) {
		int length = 0;
		for (ByteBuffer buffer : buffers) {
			length += 4;
			length += buffer.remaining();
		}
		length += 4;
		return length;
	}

	static List<ByteBuffer> readBuffers(InputStream in) throws IOException {
		List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		while (true) {
			int length = (in.read() << 24) + (in.read() << 16) + (in.read() << 8) + in.read();
			if (length == 0) { // end of buffers
				return buffers;
			}
			ByteBuffer buffer = ByteBuffer.allocate(length);
			while (buffer.hasRemaining()) {
				int p = buffer.position();
				int i = in.read(buffer.array(), p, buffer.remaining());
				if (i < 0)
					throw new EOFException("Unexpected EOF");
				buffer.position(p + i);
			}
			buffer.flip();
			buffers.add(buffer);
		}
	}

	static void writeBuffers(List<ByteBuffer> buffers, OutputStream out) throws IOException {
		for (ByteBuffer buffer : buffers) {
			writeLength(buffer.limit(), out); // length-prefix
			out.write(buffer.array(), buffer.position(), buffer.remaining());
			buffer.position(buffer.limit());
		}
		writeLength(0, out); // null-terminate
	}

	private static void writeLength(int length, OutputStream out) throws IOException {
		out.write(0xff & (length >>> 24));
		out.write(0xff & (length >>> 16));
		out.write(0xff & (length >>> 8));
		out.write(0xff & length);
	}

}
