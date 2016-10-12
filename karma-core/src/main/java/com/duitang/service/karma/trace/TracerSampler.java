package com.duitang.service.karma.trace;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public interface TracerSampler {

	/**
	 * no parameters sampling
	 * 
	 * @return
	 */
	boolean sample();

	/**
	 * sampling with parameters
	 * 
	 * @param clazzName
	 * @param method
	 * @param params
	 * @return
	 */
	boolean sample(String clazzName, String method, Object[] params);

}
