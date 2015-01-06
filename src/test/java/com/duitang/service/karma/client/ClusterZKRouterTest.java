package com.duitang.service.karma.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterZKRouterTest {

	static ObjectMapper mapper = new ObjectMapper();

	// @Test
	public void test0() {
		NavigableMap<Float, String> d = new TreeMap<Float, String>();
		d.put(0f, "a");
		d.put(0.1f, "b");
		d.put(0.3f, "c");
		d.put(0.6f, "d");
		System.out.println(d.get(d.floorKey(0f)));
		System.out.println(d.get(d.floorKey(0.05f)));
		System.out.println(d.get(d.floorKey(0.2f)));
		System.out.println(d.get(d.floorKey(0.4f)));
		System.out.println(d.get(d.floorKey(0.9f)));

		Map<String, Integer> data = new HashMap<String, Integer>();
		data.put("a", 1);
		data.put("b", 3);
		data.put("c", 6);
		data.put("d", 10);
		ClusterZKRouter r = ClusterZKRouter.createRouter("dev", data);
		d = r.createLoadMap(data);
		System.out.println(d);
		System.out.println(d.get(d.floorKey(0f)));
		System.out.println(d.get(d.floorKey(0.05f)));
		System.out.println(d.get(d.floorKey(0.2f)));
		System.out.println(d.get(d.floorKey(0.4f)));
		System.out.println(d.get(d.floorKey(0.9f)));

		data = new HashMap<String, Integer>();
		data.put("a", 3);
		data.put("b", 3);
		d = r.createLoadMap(data);
		System.out.println(d);
	}

	// @Test
	public void test1() {
		Map<String, Integer> data = null;
		NavigableMap<Float, String> d = null;
		ClusterZKRouter r = null;

		// data = new HashMap<String, Integer>();
		// data.put("a", 3);
		// data.put("b", 3);
		// r = new ClusterZKRouter();
		// d=r.createLoadMap(data);
		// System.out.println(d);
		//
		data = new HashMap<String, Integer>();
		data.put("a", 1);
		data.put("b", 10);
		r = ClusterZKRouter.createRouter("dev", data);
		d = r.createLoadMap(data);
		System.out.println(d);

		data = new HashMap<String, Integer>();
		data.put("a", 1);
		data.put("b", 1);
		data.put("c", 8);
		r = ClusterZKRouter.createRouter("dev", data);
		d = r.createLoadMap(data);
		System.out.println(d);

	}

	// @Test
	public void test2() throws Exception {
		ClusterZKRouter.enableZK("127.0.0.1:2181");
		Thread.sleep(1000000);
	}

	// @Test
	public void test3() throws Exception {
		List<String> hosts = Arrays.asList(new String[] { "a", "b", "c" });
		ClusterZKRouter router = ClusterZKRouter.createRouter("dev", ClusterZKRouter.fairLoad(hosts));
		System.out.println(router.iidMap);
		int loop = 1000000;
		Map<String, AtomicInteger> ddd = null;
		ddd = new HashMap<String, AtomicInteger>();
		ddd.put("a", new AtomicInteger(0));
		ddd.put("b", new AtomicInteger(0));
		ddd.put("c", new AtomicInteger(0));

		for (int i = 0; i < loop; i++) {
			String host = router.next(null);
			ddd.get(host).incrementAndGet();
		}
		System.out.println(ddd);

		Map<String, Integer> load = new HashMap<String, Integer>();
		load.put("a", 1);
		load.put("b", 1);
		load.put("c", 8);
		router.updateLoad(load);
		System.out.println(router.iidMap);

		ddd = new HashMap<String, AtomicInteger>();
		ddd.put("a", new AtomicInteger(0));
		ddd.put("b", new AtomicInteger(0));
		ddd.put("c", new AtomicInteger(0));

		for (int i = 0; i < loop; i++) {
			String host = router.next(null);
			ddd.get(host).incrementAndGet();
		}
		System.out.println(ddd);
	}

	@Test
	public void test4() throws Exception {
		ClusterZKRouter.enableZK("127.0.0.1:2181");
		Thread.sleep(500); // disable this u see fair-load
		Map<String, Integer> load = new HashMap<String, Integer>();
		load.put("a", 1);
		load.put("b", 1);
		load.put("c", 8);

		List<String> hosts = Arrays.asList(new String[] { "a", "b", "c" });
		ClusterZKRouter router = ClusterZKRouter.createRouter("dev", ClusterZKRouter.fairLoad(hosts));
		System.out.println(router.iidMap);

		Thread.sleep(3000);
		System.out.println("....");

		String s = mapper.writeValueAsString(load);
		String path = ClusterZKRouter.KARMA_CLUSTER_LOAD_NAME + "/dev";
		Stat stat = ClusterZKRouter.zkUtil.setData().forPath(path, s.getBytes());
		System.out.println(stat);

		System.out.println(router.iidMap);
	}

}
