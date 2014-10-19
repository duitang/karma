package com.duitang.service.misc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.reflect.ReflectResponder;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;

import com.duitang.service.misc.domain.MockComplex;
import com.duitang.service.misc.domain.MockObject;
import com.duitang.service.misc.domain.MockOps;
import com.duitang.service.misc.domain.MockOpsImpl;

public class TestRouter {

	final static protected ReflectData data = ReflectData.get();
	static protected Schema schema = null;
	static protected NettyServer server;
	static protected MockOps cli;

	public static void main(String[] args) {
		schema = data.getSchema(MockObject.class);
		MockObject mock = new MockObject();
		mock.setCreated(new Date());
		mock.setActive(true);
		mock.setId(1);
		mock.setName("world");
		mock.setP(0.4D);
		mock.setScore(0.2f);
		mock.setSeq(3);
		mock.setSrc("hello".getBytes());
		MockComplex some = new MockComplex();
//		mock.setSome(some);
		some.setPrimary(99);
		some.setSlave(88);
		System.out.println(mock);

		test1(mock);
		test2();
	}

	static void test1(MockObject mock) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DatumWriter<MockObject> wrt = new ReflectDatumWriter<MockObject>(schema);
		Encoder enc = EncoderFactory.get().binaryEncoder(bos, null);
		try {
			wrt.write(mock, enc);
			enc.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		DatumReader<MockObject> rd = new ReflectDatumReader<MockObject>(schema);
		Decoder dec = DecoderFactory.get().binaryDecoder(bos.toByteArray(), null);
		try {
			MockObject mock2 = rd.read(null, dec);
			System.out.println(mock2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void test2() {
		MockObject mock = new MockObject();
		mock.setCreated(new Date());
		mock.setActive(true);
		mock.setId(1);
		mock.setName("world");
		mock.setP(0.4D);
		mock.setScore(0.2f);
		mock.setSeq(3);
		mock.setSrc("hello".getBytes());
		MockComplex cc = new MockComplex();
		mock.setSome(cc);
		cc.setPrimary(99);
		cc.setSlave(88);
		System.out.println(mock);

		boot(MockOps.class, new MockOpsImpl());
		cli();

		cli.setMock("aaa", mock);
		MockObject mock2 = cli.getMock("aaa");
		System.out.println(mock2);
	}

	static void boot(Class serviceType, Object impl) {
		server = new NettyServer(new ReflectResponder(serviceType, impl), new InetSocketAddress(7777));
	}

	static void cli() {
		try {
			System.out.println("....");
			cli = ReflectRequestor.getClient(MockOps.class, new NettyTransceiver(new InetSocketAddress(7777)));
			System.out.println("....-----");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
