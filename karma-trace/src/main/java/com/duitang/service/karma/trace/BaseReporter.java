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

	@Override
	public void commit(List<TraceCell> tc) {
		ReporterSender.items.add(tc);
	}

}
