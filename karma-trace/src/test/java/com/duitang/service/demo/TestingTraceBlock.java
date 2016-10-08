/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
package com.duitang.service.demo;

import com.duitang.service.karma.trace.Finder;
import com.duitang.service.karma.trace.TraceBlock;

/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class TestingTraceBlock {

	public static void main(String[] args) throws Throwable {
		// Finder.enableZipkin(null, "http://192.168.10.216:9411");
		Finder.enableZipkin(null, "http://192.168.1.180:9411");
		Finder.enableConsole(true);

		Thread.sleep(2000);

		test2();
		Thread.sleep(3000);

		for (int i = 0; i < 5; i++) {
			test2();
		}
		Thread.sleep(3000);
	}

	static void test2() throws Throwable {
		TraceBlock tp = new TraceBlock();
		tp.setAttribute("commment1", "bilibilbili");
		tp.setAttribute("happy", "world");
		Thread.sleep(100);
		test3();
		tp.close();
	}

	static void test3() throws Throwable {
		try (TraceBlock tp = new TraceBlock()) {
			tp.setAttribute("foo", "bar");
			Thread.sleep(500);
		}
	}

}
