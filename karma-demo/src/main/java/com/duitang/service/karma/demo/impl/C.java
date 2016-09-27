/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import com.duitang.service.karma.demo.ServiceC;
import com.duitang.service.karma.demo.ServiceD;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class C implements ServiceC {

	public ServiceD D;

	@Override
	public String method_c(String p) {
		String param = p + " -> C";
		return D.method_d(param);
	}

}
