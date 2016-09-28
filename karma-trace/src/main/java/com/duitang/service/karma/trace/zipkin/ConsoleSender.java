package com.duitang.service.karma.trace.zipkin;

import java.io.IOException;
import java.util.List;

import com.duitang.service.karma.trace.ReporterSender;

import zipkin.Codec;
import zipkin.Span;
import zipkin.reporter.Callback;
import zipkin.reporter.Encoding;
import zipkin.reporter.Sender;

/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class ConsoleSender implements Sender {

	static protected Codec codec = Codec.THRIFT;

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
