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
public interface TraceCellVisitor<T> {

	T transform(TraceCell src);

	List<T> transform(List<TraceCell> src);

}
