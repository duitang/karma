package com.duitang.service.karma.support;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NodeDDTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String[] aa = { "11", "22", "33" };
		Object aaa = aa;
		System.out.println(ArrayUtils.toString(aaa));
		aaa = "aaa";
		System.out.println(ArrayUtils.toString(aaa));

		NodeDD dd = new NodeDD();
		dd.setAttr("aaa", new String[] { "aa", "bb" });
		dd.setAttr("bbb", "cc");
		System.out.println(dd.toString());

	}

}
