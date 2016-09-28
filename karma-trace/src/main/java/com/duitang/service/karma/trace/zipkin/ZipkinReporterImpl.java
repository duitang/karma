package com.duitang.service.karma.trace.zipkin;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.duitang.service.karma.trace.BaseReporter;
import com.duitang.service.karma.trace.ReporterSender;
import com.duitang.service.karma.trace.TraceCell;

import zipkin.Codec;
import zipkin.Span;
import zipkin.reporter.Callback;
import zipkin.reporter.Encoding;
import zipkin.reporter.Sender;

public class ZipkinReporterImpl extends BaseReporter {

	public static ZipkinReporterImpl console = new ZipkinReporterImpl(new ConsoleSender());

	public static class ConsoleSender implements Sender {

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
			if (ReporterSender.useConsole) {
				for (byte[] b : encodedSpans) {
					Span item = codec.readSpan(b);
					System.out.println(item);
				}
			}
		}

	}

	static protected Codec codec = Codec.THRIFT;

	protected Sender sender;

	/**
	 * notice: currently no safe insurance
	 */
	static protected Callback noop = new Callback() {

		@Override
		public void onError(Throwable t) {
			// ignore
		}

		@Override
		public void onComplete() {
			// ignore
		}

	};

	public ZipkinReporterImpl(Sender s) {
		sender = s;
	}

	@Override
	public void report(List<TraceCell> tc) {
		List<Span> spans = ZipkinUtils.fromTraceCell(tc);
		List<byte[]> buf = new LinkedList<byte[]>();
		for (Span s : spans) {
			buf.add(codec.writeSpan(s));
		}
		sender.sendSpans(buf, noop);
		// if (ReporterSender.useConsole) {
		// console.sender.sendSpans(buf, noop);
		// }
	}

}
