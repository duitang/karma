/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
package com.duitang.service.demo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author laurence
 * @since 2016年9月26日
 *
 */
public class TestEmptyMap {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> m = new HashMap<String, Integer>();
		m.put("1", 1);
		m.put(null, 2);
		System.out.println(m);
		System.out.println(m.get("1"));
		System.out.println(m.get(null));
	}

}

