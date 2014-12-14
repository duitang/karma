package com.duitang.service.mina;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.avro.ipc.PackageTester;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.filter.codec.AbstractProtocolEncoderOutput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.codecs.NettyFrameEncoder;
import com.duitang.service.demo.DemoService;

public class AvroEncoderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void test() throws Exception {
		final ByteArrayOutputStream bosNetty = new ByteArrayOutputStream();
		ByteArrayOutputStream bosMina = new ByteArrayOutputStream();

//		ByteBuf buf = Unpooled.buffer(1024);
//		Object param = new Object[] { "aaa", "bbb", 50000 };
//		NettyDataPack pack = new NettyDataPack();
//		pack.setSerial(1);
//		List<ByteBuffer> data1 = PackageTester.mock(DemoService.class, "memory_setString", param);
//		pack.setDatas(data1);
//
//		NettyDataPack dp = new NettyDataPack();
//		dp.setSerial(1);
//		List<ByteBuffer> data2 = PackageTester.mock(DemoService.class, "memory_setString", param);
//		dp.setDatas(data2);
//
//		DummyNettyEncode nenc = new DummyNettyEncode(bosNetty);
//		nenc.encode(null, pack, buf);
//
//		byte[] d1 = bosNetty.toByteArray();
//		System.out.println("netty bytes size: " + d1.length);
//		System.out.println(Arrays.toString(d1));
//
//		pack.setDatas(data2); // because will be flip
//		AvroEncoder encoder = new AvroEncoder();
//		DummyEncoderOutput out = new DummyEncoderOutput();
//		encoder.encode(null, dp, out);
//		out.mergeAll();
//		IoBuffer item = (IoBuffer) out.getMessageQueue().poll();
//
//		bosMina.write(item.array(), item.arrayOffset(), item.remaining());
//		byte[] d2 = bosMina.toByteArray();
//
//		System.out.println("mina bytes size: " + d2.length);
//		System.out.println(Arrays.toString(d2));
//
//		Assert.assertEquals(Arrays.toString(d1), Arrays.toString(d2));
	}

	@Test
	public void testHugeObject() throws Exception {
		int sz = 50000;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sz; i++) {
			sb.append("a");
		}
		NettyDataPack p1 = getData(DemoService.class, "memory_setString", new Object[] { "aaa", sb.toString(), 50000 });
		byte[] d1 = fromAvroNetty(p1);
		System.out.println("netty bytes size: " + d1.length);
		System.out.println(Arrays.toString(d1));

		NettyDataPack p2 = getData(DemoService.class, "memory_setString", new Object[] { "aaa", sb.toString(), 50000 });
		byte[] d2 = fromMina(p2);
		System.out.println("mina bytes size: " + d2.length);
		System.out.println(Arrays.toString(d2));

		Assert.assertEquals(Arrays.toString(d1), Arrays.toString(d2));
	}

	protected byte[] fromAvroNetty(NettyDataPack pack) throws Exception {
//		ByteBuf buf = Unpooled.buffer(102400);
//		ByteArrayOutputStream bosNetty = new ByteArrayOutputStream();
//		DummyNettyEncode nenc = new DummyNettyEncode(bosNetty);
//		nenc.encode(null, pack, buf);
//
//		byte[] d1 = bosNetty.toByteArray();
//		System.out.println("netty bytes size: " + d1.length);
//		System.out.println(Arrays.toString(d1));
//
//		return d1;
		return null;
	}

	protected byte[] fromMina(NettyDataPack pack) throws Exception {
		ByteArrayOutputStream bosMina = new ByteArrayOutputStream();
		AvroEncoder encoder = new AvroEncoder();
		DummyEncoderOutput out = new DummyEncoderOutput();
		encoder.encode(null, pack, out);
		out.mergeAll();
		IoBuffer item = (IoBuffer) out.getMessageQueue().poll();

		bosMina.write(item.array(), item.arrayOffset(), item.remaining());
		byte[] d2 = bosMina.toByteArray();
		return d2;
	}

	protected NettyDataPack getData(Class clz, String name, Object param) throws Exception {
		NettyDataPack pack = new NettyDataPack();
		pack.setSerial(Integer.MAX_VALUE);
		List<ByteBuffer> data1 = PackageTester.mock(clz, name, param);
		pack.setDatas(data1);
		return pack;
	}

}

class DummyEncoderOutput extends AbstractProtocolEncoderOutput {

	@Override
	public WriteFuture flush() {
		return null;
	}

}

class DummyNettyEncode extends NettyFrameEncoder {

	protected OutputStream os;

	public DummyNettyEncode(OutputStream os) {
		this.os = os;
	}

//	public void encode(ChannelHandlerContext ctx, NettyDataPack msg, ByteBuf out) throws Exception {
//		super.encode(ctx, msg, out);
//		out.getBytes(out.readerIndex(), os, out.readableBytes());
//	}
}