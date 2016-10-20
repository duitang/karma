package com.duitang.service.karma.support;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServicesExporterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		List<String> names;
		ServicesExporter exporter = new ServicesExporter();

		exporter.setEnabledPt(Arrays.asList("BigBig"));
		exporter.setDisabledPt(Arrays.asList("BigBigBig", "^java"));

		exporter.setPort(8989);
		exporter.setServices(Arrays.asList(new A(), new B(), new C()));
		exporter.init();

		names = exporter.getExportedInterfaces();
		System.out.println(names);
		Assert.assertTrue(names.contains(BigBig.class.getName()));
		Assert.assertFalse(names.contains(BigBigBig.class.getName()));
		Assert.assertFalse(names.contains(Iterable.class.getName()));

		exporter.halt();

		exporter = new ServicesExporter();

		exporter.setEnabledPt(Arrays.asList("BigBig"));
		exporter.setDisabledPt(Arrays.asList("BigBigBig", "^java"));

		exporter.setPort(8989);
		exporter.setServices(Arrays.asList((Object) new D()));
		exporter.init();

		names = exporter.getExportedInterfaces();
		System.out.println(names);
		Assert.assertTrue(names.contains(BigBig.class.getName()));
		Assert.assertFalse(names.contains(BigBigBig.class.getName()));
		Assert.assertFalse(names.contains(Iterable.class.getName()));

		exporter.halt();

		exporter = new ServicesExporter();

		exporter.setPort(8989);
		exporter.setServices(Arrays.asList((Object) new D()));
		exporter.init();

		names = exporter.getExportedInterfaces();
		System.out.println(names);
		Assert.assertTrue(names.contains(BigBig.class.getName()));
		Assert.assertTrue(names.contains(BigBigBig.class.getName()));
		Assert.assertFalse(names.contains(Iterable.class.getName()));

		exporter.halt();

	}

	class A implements java.lang.Iterable<A> {

		@Override
		public Iterator<A> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	class B implements BigBig {

		@Override
		public void hell1() {

		}

	}

	class C implements BigBigBig {

		@Override
		public void hell() {
			// TODO Auto-generated method stub

		}

	}

	class D implements java.lang.Iterable<D>, BigBigBig, BigBig {

		@Override
		public void hell1() {
			// TODO Auto-generated method stub

		}

		@Override
		public void hell() {
			// TODO Auto-generated method stub

		}

		@Override
		public Iterator<D> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

	}

}

interface BigBigBig {
	void hell();
}

interface BigBig {
	void hell1();
}
