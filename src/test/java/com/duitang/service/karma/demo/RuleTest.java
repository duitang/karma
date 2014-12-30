package com.duitang.service.karma.demo;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.duitang.service.karma.demo.domain.SimpleObject;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RuleTest {

	static ObjectMapper mapper = new ObjectMapper();

	// @Test
	public void test0() throws Exception {
		String src = "{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": \"bbb\"}, \"b\": [1, 2]}";
		SimpleObject obj = new SimpleObject();
		Map mm = mapper.readValue(src, HashMap.class);
		BeanUtils.populate(obj, mm);
		System.out.println(obj);
	}

	// @Test
	public void test1() throws Exception {
		String src = "[{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": \"bbb\"}, \"b\": [1, 2]},{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": \"bbb\"}, \"b\": [1, 2]}]";
		ArrayList<Map> mm = mapper.readValue(src, ArrayList.class);
		ArrayList ret = new ArrayList();
		for (Map m : mm) {
			SimpleObject obj = new SimpleObject();
			BeanUtils.populate(obj, m);
			ret.add(obj);
			System.out.println(obj);
		}
		System.out.println(ret);
	}

	@Test
	public void test2() {
		ArrayList<String> ss = new ArrayList<String>();
		System.out.println(ss.getClass().getTypeParameters()[0]);
		Class persistentClass = (Class) ((ParameterizedType) ss.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		System.out.println(persistentClass.getName());
		ParameterizedType tp = (ParameterizedType) ss.getClass().getGenericSuperclass();
		System.out.println(tp.getActualTypeArguments()[0]);
	}

}
