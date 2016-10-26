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
	 * @param i
	 * @param resp1
	 * @param ok1
	 */
	public void updateResponse(int i, float resp1, boolean ok1);

	/**
	 * update load information
	 * 
	 * @param active
	 */
	public void updateLoad(float[] load);

	/**
	 * reload with candidates
	 */
	void reload(float[] samples);

	/**
	 * get current weights for sampling
	 * 
	 * @return
	 */
	float[] getWeights();

	/**
	 * get statistics
	 * 
	 * @return
	 */
	String[] getDebugInfo();

}
