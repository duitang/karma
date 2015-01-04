package com.duitang.service.karma.client;

public interface IOBalance {

	/**
	 * fetch next token from router
	 * 
	 * @param token
	 *            current used token
	 * @return next token
	 */
	public String next(String token);

}
