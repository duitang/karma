package com.duitang.service.karma.client;

import java.util.List;

import com.duitang.service.karma.trace.TraceCell;

/**
 * for backend load balance
 * 
 * @author laurence
 * 
 */
public interface IOBalance {

	/**
	 * fetch next token from router
	 *
	 * @param token
	 *            current used token
	 * @return next token
	 */
	public String next(String token);

	/**
	 * filling trace into IOSession
	 * 
	 * @param token
	 * @param tc
	 */
	public void traceFeed(String token, TraceCell tc);

	/**
	 * update nodes
	 * 
	 * @param nodes
	 */
	public void setNodes(List<String> nodes);

}
