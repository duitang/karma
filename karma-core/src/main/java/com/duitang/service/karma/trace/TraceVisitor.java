package com.duitang.service.karma.trace;

import java.util.List;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public interface TraceVisitor {

	/**
	 * send out trace cell
	 * 
	 * @param tc
	 */
	void visit(TraceCell tc);

	/**
	 * send out batch trace cell
	 * 
	 * @param tcs
	 */
	void visits(List<TraceCell> tcs);

}
