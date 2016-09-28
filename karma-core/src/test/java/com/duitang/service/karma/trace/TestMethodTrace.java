/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
package com.duitang.service.karma.trace;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author laurence
 * @since 2016年9月28日
 *
 */
public class TestMethodTrace {

	/**
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		Logger root = (Logger) LoggerFactory.getLogger(NoopTraceVisitor.class);
		root.setLevel(Level.DEBUG);

		test2();
	}

	static void test1() throws Throwable {
		TracePoint tp = new TracePoint();
		Thread.sleep(2000);
		tp.close();
	}

	static void test2() throws Throwable {
		TracePoint tp = new TracePoint();
		Thread.sleep(2000);
		test3();
		tp.close();
	}

	static void test3() throws Throwable {
		TracePoint tp = new TracePoint();
		Thread.sleep(1000);
		tp.close();
	}

}
