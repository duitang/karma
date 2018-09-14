/**
 * @author laurence
 * @since 2016年9月29日
 *
 */
package com.duitang.service.karma.client;

/**
 * 
 * <pre>
 * sample item from candidates
 * 1. reload with some candidates
 * 2. checkpoint for generate sample probabilities
 * 3. sample
 * 4. update candidate by index
 * 5. loop from 3 again and again until some moment
 * 6. from 2 again
 * </pre>
 * 
 * @author laurence
 * @since 2016年9月29日
 *
 */
public interface BalancePolicy {

	/**
	 * period for policy updater
	 */
	void checkpoint();

	/**
	 * get next
	 */
	int sample();

	/**
	 * total candidates
	 */
	int size();

	/**
	 * update response statistics
	 * 
	 * @param i which one
	 * @param resp1 performance
	 * @param ok1 well done
	 */
	void updateResponse(int i, float resp1, boolean ok1);

	/**
	 * update load information
	 * 
	 * @param i which one
	 * @param load performance
	 */
	void updateLoad(int i, float load);

	/**
	 * reload with candidates
	 * @param samples initial weights
	 */
	void reload(float[] samples);

	/**
	 * get current weights for sampling
	 * 
	 * @return weights
	 */
	float[] getWeights();

	/**
	 * get statistics
	 * 
	 * @return debug information
	 */
	String[] getDebugInfo();

	/**
	 * get all nodes' load info
	 * 
	 * @return performance
	 */
	float[] getLoads();

}
