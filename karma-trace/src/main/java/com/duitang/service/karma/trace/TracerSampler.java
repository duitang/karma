package com.duitang.service.karma.trace;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public interface TracerSampler {

	boolean sample(String clazzName, String method, Object[] params);

}
