package com.duitang.service.karma.support;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegistryInfoTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRegistry() {
		LinkedHashMap<String, Float> nodes = new LinkedHashMap<>();
		nodes.put("aa:11", 11f);
		nodes.put("bb:22", 22f);
		nodes.put("bb:33", 33f);
		RegistryInfo ret0 = new RegistryInfo(false, nodes);

		System.out.println(ret0.toString());

		List<RPCNode> the_nodes = ret0.hashing.getNodes();

		RegistryInfo ret1 = new RegistryInfo(false, the_nodes);
		System.out.println(ret1.toString());

		Assert.assertEquals(ret0, ret1);

		Assert.assertNotNull(ret0.getNode("aa:11"));
		Assert.assertNull(ret0.getNode("aaa:11"));

	}

}
