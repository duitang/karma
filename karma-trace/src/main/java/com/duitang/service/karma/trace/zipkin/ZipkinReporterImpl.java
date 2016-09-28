package com.duitang.service.karma.trace.zipkin;

import java.util.LinkedList;
import java.util.List;

import com.duitang.service.karma.trace.BaseReporter;
import com.duitang.service.karma.trace.TraceCell;

import zipkin.Codec;
import zipkin.Span;
import zipkin.reporter.Callback;
import zipkin.reporter.Sender;

public class ZipkinReporterImpl extends BaseReporter {

	Codec codec = Codec.THRIFT;
	protected Sender sender;
	protected TraceCell2Span transformer;

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
		this(s, null);
	}

	public ZipkinReporterImpl(Sender s, TraceCell2Span transformer) {
		this.sender = s;
		this.transformer = transformer == null ? new TraceCell2Span() : transformer;
	}

	@Override
	protected void report0(List<TraceCell> tc) {
		List<Span> spans = transformer.transform(tc);
		List<byte[]> buf = new LinkedList<byte[]>();
		for (Span s : spans) {
			buf.add(codec.writeSpan(s));
		}
		sender.sendSpans(buf, noop);
	}

}
