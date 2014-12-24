package com.duitang.service.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.duitang.service.demo.domain.ComplexObject;
import com.duitang.service.demo.domain.OnlyList;
import com.duitang.service.demo.domain.SimpleObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class XStreamTest {
	XStream xstream = new XStream(new StaxDriver());

	// @Test
	public void test0() {
		XStream xstream = new XStream(new StaxDriver());
		DemoObject ret = new DemoObject();
		ret.setB_v(true);
		ret.setBs_v("abcd1234".getBytes());
		ret.setF_v(1.2f);
		ret.setI_v(23);
		ret.setL_v(123);
		ret.setM_v(new HashMap());
		ret.getM_v().put("11", "22");
		ret.setDomain("aaa");
		ret.setMethod("bbb");

		String xml = xstream.toXML(ret);
		System.out.println(xml);
		Object ddd = xstream.fromXML(xml);
		System.out.println(ddd);
		System.out.println(ddd.getClass().getName());

		List<DemoObject> lst = new ArrayList<DemoObject>();
		lst.add(ret);
		lst.add(ret);
		String sss = xstream.toXML(lst);
		System.out.println(sss);
		ddd = xstream.fromXML(sss);
		System.out.println(ddd.getClass().getName());
		System.out.println(ddd);

		Integer a = 112;
		System.out.println(xstream.toXML(a));
		Object[] param = new Object[] { 1L, 2F, 3D, "aaa".getBytes(), ret };
		byte[] bb = "aaa".getBytes();
		// YWFh
		System.out.println(Base64.encodeBase64String(bb));
		System.out.println(xstream.toXML(param));

	}

	// @Test
	public void test1() {
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

		XStream xstream = new XStream(new StaxDriver());
		System.out.println(xstream.toXML(obj));

		// String src =
		// "<?xml version=\"1.0\" ?><com.duitang.service.demo.domain.ComplexObject><a>1</a><b>2.1</b><c>3</c><d>4.3</d><e>true</e><f>YWFh</f><g><a>bbb</a><b class=\"java.util.Arrays$ArrayList\"><a class=\"java.lang.Float-array\"><float>5.5</float><float>6.6</float></a></b><c><entry><string>a22</string><double>222.2</double></entry><entry><string>a11</string><double>111.1</double></entry></c></g></com.duitang.service.demo.domain.ComplexObject>";
		String src = "<?xml version=\"1.0\" ?><com.duitang.service.demo.domain.ComplexObject><a>1</a><b>2.1</b><c>3</c><d>4.3</d><e>true</e><f>YWFh</f><g><a>bbb</a><b class=\"java.util.Arrays$ArrayList\"><a class=\"java.lang.Float-array\"><float>5.5</float><float>6.6</float></a></b><c><entry><string>a22</string><double>222.2</double></entry><entry><string>a11</string><double>111.1</double></entry></c></g></com.duitang.service.demo.domain.ComplexObject>";

	}

	@Test
	public void test2() {
		SimpleObject so = new SimpleObject();
		so.setA("bbb");
		so.setB(Arrays.asList(new Float[] { 5.5f, 6.6f }));
		so.setC(new HashMap<String, Double>());
		so.getC().put("a11", 111.1D);
		so.getC().put("a22", 222.2D);
		System.out.println(xstream.toXML(so));
		String src = "<?xml version=\"1.0\" ?><com.duitang.service.demo.domain.SimpleObject><a>bbb</a><b class=\"java.util.Arrays$ArrayList\"><a class=\"java.lang.Float-array\"><float>5.5</float><float>6.6</float></a></b><c><entry><string>a22</string><double>222.2</double></entry><entry><string>a11</string><double>111.1</double></entry></c></com.duitang.service.demo.domain.SimpleObject>";
		Object ddd = xstream.fromXML(src);
		src = "<?xml version=\"1.0\" ?><com.duitang.service.demo.domain.SimpleObject><a>bbb</a><b class=\"java.util.Arrays$ArrayList\"><a class=\"java.lang.Float-array\"><float>5.5</float><float>6.6</float></a></b><c><entry><string>a22</string><double>222.2</double></entry><entry><string>a11</string><double>111.1</double></entry></c></com.duitang.service.demo.domain.SimpleObject>";
		System.out.println(ddd);
		System.out.println(xstream.toXML(new long[] { 1, 2 }));
		ArrayList<Long> lst = new ArrayList<Long>();
		lst.add(1L);
		lst.add(2L);
		System.out.println(xstream.toXML(new long[] { 1, 2 }));
		System.out.println(xstream.toXML(Arrays.asList(new Long[] { 1L, 2L })));
		System.out.println(xstream.toXML(lst));

		src = "<?xml version=\"1.0\" ?><list><long>1</long><long>2</long></list>";
		Object oo = xstream.fromXML(src);
		System.out.println(oo);
		System.out.println(oo.getClass().getName());

		Long[] aa = { 1L, 2L };
		System.out.println(xstream.toXML(aa));
		src = "<?xml version=\"1.0\" ?><com.duitang.service.demo.domain.OnlyList><data><long>1</long><long>2</long></data></com.duitang.service.demo.domain.OnlyList>";
		System.out.println(xstream.fromXML(src));
		ddd = xstream.fromXML(src);
		System.out.println((((OnlyList) ddd).getData()).getClass().getName());
		
		
	}
}
