package com.duitang.service.karma.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.duitang.service.karma.client.impl.NaiveBalancer;

public class KarmaIORouterTest {

	@Test
	public void test() {
		List<String> lst = Arrays.asList(new String[] { "a", "b", "c", "d", "e", "f" });

		StringBuilder sb = new StringBuilder();
		NaiveBalancer router = new NaiveBalancer(lst);
		String s = null;
		for (int i = 0; i < lst.size() * 1000 + 1; i++) {
			s = router.next(null);
			sb.append(s);
			System.out.print(s);
		}

		System.out.println();
		System.out.println(calc(sb));
	}

	Map<Character, AtomicInteger> calc(StringBuilder sb) {
		Map<Character, AtomicInteger> ret = new HashMap<Character, AtomicInteger>();
		for (char ch : sb.toString().toCharArray()) {
			if (!ret.containsKey(Character.valueOf(ch))) {
				ret.put(Character.valueOf(ch), new AtomicInteger(0));
			}
			ret.get(Character.valueOf(ch)).incrementAndGet();
		}
		return ret;
	}

}
