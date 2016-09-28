/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
package com.duitang.service.karma.trace;

import java.util.List;

import com.duitang.service.karma.trace.zipkin.TraceCell2Span;

/**
 * <pre>
 *	Zipkin Trace简单好用，借用它做Console Oupput
 * </pre>
 * 
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class ConsoleReporter extends BaseReporter {

	protected TraceCellVisitor transformer = new TraceCell2Span();

	public void setTransformer(TraceCellVisitor transformer) {
		this.transformer = transformer;
	}

	@Override
	public void report(List<TraceCell> tc) {
		List spans = transformer.transform(tc);
		if (ReporterSender.useConsole) {
			for (Object o : spans) {
				System.out.println(o);
			}
		}
	}

	@Override
	protected void report0(List<TraceCell> tc) {
		// ignore
	}

}
