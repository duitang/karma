/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
package com.duitang.service.demo;

import com.duitang.service.karma.trace.Finder;
import com.duitang.service.karma.trace.TracePoint;

/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class TestingAutoTrace {

	public static void main(String[] args) throws Throwable {
		Finder.enableZipkin(null, "http://192.168.10.216:9411");
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
		TracePoint tp = new TracePoint();
		Thread.sleep(100);
		test3();
		tp.close();
	}

	static void test3() throws Throwable {
		TracePoint tp = new TracePoint();
		Thread.sleep(500);
		tp.close();
	}

}
