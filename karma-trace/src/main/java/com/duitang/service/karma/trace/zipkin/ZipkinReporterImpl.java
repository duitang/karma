package com.duitang.service.karma.trace.zipkin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.duitang.service.karma.support.IPUtils;
import com.duitang.service.karma.trace.TraceCell;
import com.duitang.service.karma.trace.TracerReporter;

import zipkin.Codec;
import zipkin.Span;
import zipkin.reporter.Callback;
import zipkin.reporter.Encoding;
import zipkin.reporter.Sender;
import zipkin.reporter.kafka08.KafkaSender;
import zipkin.reporter.libthrift.LibthriftSender;
import zipkin.reporter.libthrift.LibthriftSender.Builder;
import zipkin.reporter.okhttp3.OkHttpSender;

public class ZipkinReporterImpl implements TracerReporter {

	protected BlockingQueue<List<byte[]>> items = new LinkedBlockingQueue<>();
	protected Thread reporterDaemon = new Thread() {

		@Override
		public void run() {
			while (true) {
				try {
					List<byte[]> item = items.take();
					if (sender != null) {
						sender.sendSpans(item, noop);
					}
					if (useConsole) {
						console_1.sendSpans(item, noop);
					}
				} catch (InterruptedException e) {
					//
				}
			}
		}

	};

	protected Sender sender;
	protected Codec codec;

	public static boolean useConsole = false;
	protected static ConsoleSender console_1 = new ConsoleSender();

	/**
	 * notice: currently no safe insurance
	 */
	protected Callback noop = new Callback() {

		@Override
		public void onError(Throwable t) {
			// ignore
		}

		@Override
		public void onComplete() {
			// ignore
		}

	};

	static class ConsoleSender implements Sender {

		@Override
		public CheckResult check() {
			return null;
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public Encoding encoding() {
			return null;
		}

		@Override
		public int messageMaxBytes() {
			return 0;
		}

		@Override
		public int messageSizeInBytes(List<byte[]> encodedSpans) {
			return 0;
		}

		@Override
		public void sendSpans(List<byte[]> encodedSpans, Callback callback) {
			for (byte[] b : encodedSpans) {
				Span item = Codec.THRIFT.readSpan(b);
				System.out.println(item);
			}
		}

	}

	public ZipkinReporterImpl(String url) throws URISyntaxException {
		reporterDaemon.setDaemon(true);
		reporterDaemon.start();
		codec = Codec.THRIFT;
		if (url == null || url.toLowerCase().equals("console")) {
			useConsole = true;
			sender = null;
		}
		if (url.startsWith("kafka://")) {
			sender = KafkaSender.create(url.substring("kafka://".length()));
		}
		if (url.startsWith("thrift://")) {
			String u = url.substring("thrift://".length());
			Builder bd = LibthriftSender.builder().host(IPUtils.getHost(u));
			Integer port = IPUtils.getPort(u);
			if (port != null) {
				bd.port(port);
			}
			sender = bd.build();
		}
		if (url.startsWith("http://")) {
			String path = "/api/v1/spans";
			sender = OkHttpSender.builder().compressionEnabled(true).endpoint(url + path).build();
		}
		if (sender == null) {
			String only = "( kafka:// | thrift:// | console )";
			throw new RuntimeException("not valid: " + url + " ; please using prefix => " + only);
		}
	}

	@Override
	public void report(List<TraceCell> tc) {
		asyncReport(tc);
	}

	@Override
	public void asyncReport(List<TraceCell> tc) {
		List<Span> spans = ZipkinUtils.fromTraceCell(tc);
		List<byte[]> buf = new LinkedList<byte[]>();
		for (Span s : spans) {
			buf.add(codec.writeSpan(s));
		}
		try {
			items.put(buf);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
