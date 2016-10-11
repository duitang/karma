package com.duitang.service.karma.support;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RPCNodeHashingTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetRawConnURL() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRawConnSchema() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSafeConnURL() {
		fail("Not yet implemented");
	}

	@Test
	public void testCompareTo() {
		List<String> nodes = Arrays.asList("aa:11", "bb:22", "cc:33", "dd:44");
		RPCNodeHashing a = RPCNodeHashing.createFromString(nodes);
		RPCNodeHashing b = RPCNodeHashing.createFromString(nodes);
		
		boolean r = a.equals(b);
		Assert.assertTrue(r);

	}

	@Test
	public void testEqualsObject() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetURLs() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetSchema() {
		fail("Not yet implemented");
	}

	@Test
	public void testSortNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateFromString() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateFromNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateFromHashMap() {
		fail("Not yet implemented");
	}

	@Test
	public void testReverseToMap() {
		fail("Not yet implemented");
	}

}
