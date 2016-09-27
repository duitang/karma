/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import com.duitang.service.karma.demo.ServiceE;
import com.duitang.service.karma.demo.ServiceF;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class E implements ServiceE {

	public ServiceF F;

	@Override
	public String method_e(String p) {
		String param = p + " -> E";
		return F.method_f(param);
	}

}
