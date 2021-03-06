/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
package com.duitang.service.karma.trace;

import java.util.List;

/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
public abstract class BaseReporter implements TracerReporter {

	abstract protected void report0(List<TraceCell> tc);

	@Override
	public void report(List<TraceCell> tc) {
		report0(tc);
		if (ReporterSender.useConsole) {
			ReporterSender.console.report(tc);
		}
	}

	@Override
	public void commit(List<TraceCell> tc) {
		ReporterSender.items.add(tc);
	}

}
