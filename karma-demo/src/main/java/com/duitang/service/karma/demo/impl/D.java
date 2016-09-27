/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import com.duitang.service.karma.demo.ServiceD;
import com.duitang.service.karma.demo.ServiceE;
import com.duitang.service.karma.demo.ServiceF;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class D implements ServiceD {

	public ServiceE E;
	public ServiceF F;

	@Override
	public String method_d(String p) {
		String param = p + " -> D";
		return E.method_e(param) + F.method_f(param);
	}

}
