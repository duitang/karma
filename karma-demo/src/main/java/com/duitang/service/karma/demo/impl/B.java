/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import com.duitang.service.karma.demo.ServiceB;
import com.duitang.service.karma.demo.ServiceC;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class B implements ServiceB {

	public ServiceC C;

	@Override
	public String method_b(String p) {
		String param = p + " -> B";
		return C.method_c(param);
	}

}
