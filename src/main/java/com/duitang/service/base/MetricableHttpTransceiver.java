package com.duitang.service.base;

import java.io.ByteArrayInputStream;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.ipc.Transceiver;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;

public class MetricableHttpTransceiver extends Transceiver implements Closeable {

	static final String CONTENT_TYPE = "avro/binary";

	static final Collection<String> content_type = Arrays.asList(new String[] { CONTENT_TYPE });

	static protected int timeout = 500;
	static protected int cpu_affinity = Double.valueOf(Runtime.getRuntime().availableProcessors() * 1.5).intValue();
	static protected List<HttpClient> client = new ArrayList<HttpClient>();
	static protected AtomicInteger roundrobin = new AtomicInteger(0);
	static protected int rsize;

	protected URL url;
	protected ContentResponse resp;
	protected String clientid;
	protected Exception eer;
	protected boolean done = false;

	static {
		initClient();
	}

	static public void setTimeout(int timeout) {
		MetricableHttpTransceiver.timeout = timeout;
		initClient();
	}

	static public void initClient() {
		for (int i = 0; i < cpu_affinity; i++) {
			HttpClient cli = new HttpClient();
			cli.setFollowRedirects(false);
			cli.setIdleTimeout(timeout);
			try {
				cli.start();
			} catch (Exception e) {
				throw new RuntimeException("metricable http transceiver start http client error: ", e);
			}
			client.add(cli);
		}
		rsize = client.size();
	}

	public MetricableHttpTransceiver(String clientid, URL url) {
		this.clientid = clientid;
		this.url = url;
	}

	public String getRemoteName() {
		return this.url.toString();
	}

	public synchronized List<ByteBuffer> readBuffers() throws IOException {
		InputStream in = null;
		try {
			if (resp == null) {
				throw new IOException(eer);
			}
			in = new ByteArrayInputStream(resp.getContent());
			return readBuffers(in);
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			if (in != null) {
				in.close();
			}
			eer = null;
		}
	}

	@Override
	public void close() throws IOException {
		if (resp == null) {
			return;
		}
	}

	public synchronized void writeBuffers(List<ByteBuffer> buffers) throws IOException {
		int rid = roundrobin.incrementAndGet();
		HttpClient cli = client.get(rid % rsize);
		Request r = cli.newRequest(getRemoteName()).method("POST");
		r.getHeaders().add(new HttpField(HttpHeader.CONTENT_TYPE, CONTENT_TYPE));
		r.getHeaders().add(new HttpField(HttpHeader.CONTENT_LENGTH, Integer.toString(getLength(buffers))));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			writeBuffers(buffers, out);
			r.content(new BytesContentProvider(out.toByteArray()), CONTENT_TYPE);
			try {
				resp = r.send();
			} catch (Exception e) {
				resp = null;
				eer = e;
			}
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
