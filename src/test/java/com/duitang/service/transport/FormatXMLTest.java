package com.duitang.service.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.demo.domain.ComplexObject;
import com.duitang.service.demo.domain.SimpleObject;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FormatXMLTest {

	static ObjectMapper mapper = new ObjectMapper();

	ComplexObject ddd;

	@Before
	public void setUp() {
		ComplexObject obj = new ComplexObject();
		obj.setA(1);
		obj.setB(2.1f);
		obj.setC(3);
		obj.setD(4.3d);
		obj.setE(true);
		obj.setF("aaa".getBytes());
		obj.setG(new SimpleObject());
		obj.getG().setA("bbb");
		obj.getG().setB(Arrays.asList(new Float[] { 5.5f, 6.6f }));
		obj.getG().setC(new HashMap<String, Double>());
		obj.getG().getC().put("a11", 111.1D);
		obj.getG().getC().put("a22", 222.2D);

		ddd = obj;
	}

	// @Test
	public void test() throws Exception {
		SimpleObject obj = new SimpleObject();

		ArrayList b = new ArrayList();
		b.add(11f);
		HashMap c = new HashMap();
		c.put("dd", 12.3D);
		HashMap data = new HashMap();
		data.put("a", "bbb");
		data.put("b", b);
		data.put("c", c);
		BeanUtils.populate(obj, data);
		System.out.println(obj);

		String jobj = mapper.writeValueAsString(obj);
		System.out.println(jobj);

		HashMap mm = mapper.readValue(jobj, HashMap.class);
		System.out.println(mm);
		SimpleObject obj2 = new SimpleObject();
		BeanUtils.populate(obj2, mm);
		System.out.println(obj2);
	}

	// @Test
	public void test2() throws Exception {
		String jobj = mapper.writeValueAsString(ddd);
		HashMap mm = mapper.readValue(jobj, HashMap.class);
		ComplexObject ret = new ComplexObject();
		BeanUtils.populate(ret, mm);
		System.out.println(ret);
	}

	@Test
	public void test3() throws Exception {
		long ts = System.currentTimeMillis();
		int loop = 100000;
		for (int i = 0; i < loop; i++) {
			String src = "[{\"a\": \"hello\", \"c\": {\"dd\": 23, \"aaa\": \"bbb\"}, \"b\": [1, 2]}]";
			ArrayList mm = mapper.readValue(src, ArrayList.class);
//			System.out.println(mm.get(0));
			// System.out.println(mm.get(1));
			// System.out.println(mm.get(2));
			SimpleObject obj2 = new SimpleObject();
			BeanUtils.populate(obj2, (Map) mm.get(0));
//			System.out.println(obj2);
		}
		ts = System.currentTimeMillis() - ts;
		System.out.println("Time elapsed: " + ts + "ms");
	}

}
