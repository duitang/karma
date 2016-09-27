/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import com.duitang.service.karma.demo.ServiceA;
import com.duitang.service.karma.demo.ServiceB;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class A implements ServiceA {

	public ServiceB B;

	@Override
	public String method_a(String p) {
		String param = p + " -> A";
		return B.method_b(param);
	}

}
