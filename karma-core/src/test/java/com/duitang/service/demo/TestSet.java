/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
package com.duitang.service.demo;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
public class TestSet {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HashSet<String> n0 = new HashSet<String>(Arrays.asList("1","2","3","4"));
		HashSet<String> n1 = new HashSet<String>(Arrays.asList("3","4","5","6"));
		n0.addAll(n1);
		
	}

}

