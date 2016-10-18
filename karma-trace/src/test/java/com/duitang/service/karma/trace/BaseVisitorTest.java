package com.duitang.service.karma.trace;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BaseVisitorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws Exception {
		TraceContextHolder.setSampler(new AlwaysSampled());

		BaseVisitor bv = new BaseVisitor();
		MyReport sd = new MyReport();
		bv.addReporter("dev1", sd);
		Assert.assertTrue(bv.reporters.containsKey("dev1"));
		Assert.assertTrue(bv.reporters.get("dev1").contains(sd));

		TracePoint tp = new TracePoint();
		tp.tc.group = "dev1";
		Thread.sleep(100);
		tp.close();
		bv.visit(tp.tc);

		TracePoint tp1 = new TracePoint();
		tp1.tc.group = "dev1";
		Thread.sleep(50);
		tp1.close();
		TracePoint tp2 = new TracePoint();
		tp2.tc.group = "dev1";
		Thread.sleep(50);
		tp2.close();

		bv.visits(Arrays.asList(tp1.tc, tp2.tc));

		Assert.assertTrue(MyReport.commited.get() == 3);

		bv.removeReporter("dev1");
		TracePoint tp3 = new TracePoint();
		tp3.tc.group = "dev1";
		Thread.sleep(50);
		tp3.close();
		Assert.assertTrue(MyReport.commited.get() == 3);

	}

}

class MyReport implements TracerReporter {

	final static AtomicInteger reported = new AtomicInteger(0);
	final static AtomicInteger commited = new AtomicInteger(0);

	@Override
	public void report(List<TraceCell> tc) {
		reported.incrementAndGet();
		System.out.println("reported: " + tc.toString());
	}

	@Override
	public void commit(List<TraceCell> tc) {
		commited.addAndGet(tc.size());
		System.out.println("commited: " + tc);
	}

}
