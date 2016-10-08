package com.duitang.service.karma.trace;

import java.util.Map;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public interface TracerLogger {

	void log(String msg, TraceCell tc);

	void log(String msg, TraceCellVisitor<Map> visitor, TraceCell tc);

}
