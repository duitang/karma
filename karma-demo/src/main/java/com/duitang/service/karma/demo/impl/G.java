/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
package com.duitang.service.karma.demo.impl;

import com.duitang.service.karma.demo.ServiceG;

/**
 * @author laurence
 * @since 2016年9月27日
 *
 */
public class G implements ServiceG {

	@Override
	public String method_g(String p) {
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return p + " -> G";
	}

}
