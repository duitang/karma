/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
package com.duitang.service.karma.client;

import java.util.LinkedHashMap;

/**
 * @author laurence
 * @since 2016年10月1日
 *
 */
public class RegistryInfo {

	public LinkedHashMap<String, Double> wNodes;
	public boolean freezeMode;

	public static String getConnectionURL(String url) {
		if (url == null) {
			return null;
		}
		int pos = url.indexOf("://");
		if (pos > 0) {
			pos += 3;
			return url.substring(pos);
		}
		return url;
	}

}
