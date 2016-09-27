/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import com.duitang.service.karma.demo.ServiceF;
import com.duitang.service.karma.demo.ServiceG;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class F implements ServiceF {

	public ServiceG G;

	@Override
	public String method_f(String p) {
		String param = p + " -> F";
		return G.method_g(param);
	}

}
