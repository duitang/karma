package org.apache.avro.ipc;

import org.apache.avro.Protocol;
import org.apache.avro.reflect.ReflectData;
import org.junit.Test;

import com.duitang.service.demo.DemoService;

public class TestProtocol {

	@Test
	public void test1() {
		Protocol service = ReflectData.get().getProtocol(DemoService.class);
		System.out.println(service.toString(true));
	}

}
