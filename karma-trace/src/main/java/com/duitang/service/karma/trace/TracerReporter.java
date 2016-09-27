package com.duitang.service.karma.trace;

import java.util.List;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public interface TracerReporter {

	/**
	 * report item in sync mode
	 * 
	 * @param tc
	 */
	public void report(List<TraceCell> tc);

	/**
	 * report item in async mode
	 * 
	 * @param tc
	 */
	public void asyncReport(List<TraceCell> tc);

}
