/**
 * @author laurence
 * @since 2016年10月6日
 *
 */
package com.duitang.service.karma.support;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import com.sun.management.UnixOperatingSystemMXBean;

/**
 * @author laurence
 * @since 2016年10月6日
 *
 */
public class TestFileHandlers {

	public static void main(String[] args) {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		if (os instanceof UnixOperatingSystemMXBean) {
			System.out.println("Number of open fd: " + ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount());
		}
	}

}
