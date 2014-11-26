package com.duitang.service.mina;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.avro.ipc.PackageTester;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.AbstractProtocolDecoderOutput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.codecs.NettyFrameDecoder;
import com.duitang.service.demo.DemoService;

public class AvroDecoderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

//	@Test
	public void test() throws Exception {
		final ByteArrayOutputStream bosNetty = new ByteArrayOutputStream();

		ByteBuf buf = Unpooled.buffer(1024);
		Object param = new Object[] { "aaa", "bbb", 50000 };
		NettyDataPack pack = new NettyDataPack();
		pack.setSerial(1);
		List<ByteBuffer> data1 = PackageTester.mock(DemoService.class, "memory_setString", param);
		pack.setDatas(data1);

		DummyNettyEncode nenc = new DummyNettyEncode(bosNetty);
		nenc.encode(null, pack, buf);

		byte[] d1 = bosNetty.toByteArray();
		DummyNettyDecoder dec = new DummyNettyDecoder();
		ByteBuf inbuf = Unpooled.copiedBuffer(d1);
		ArrayList outter = new ArrayList();
		dec.decode(null, inbuf, outter);
		NettyDataPack data0 = (NettyDataPack) outter.get(0);
		System.out.println(data0.getSerial());
		System.out.println(data0.getDatas());

		IoBuffer inbuf2 = IoBuffer.wrap(d1);
		AvroDecoder avdec = new AvroDecoder();
		DummyDecoderOutput out2 = new DummyDecoderOutput();
		while (inbuf2.hasRemaining()) {
			avdec.decode(null, inbuf2, out2);
		}
		NettyDataPack data11 = (NettyDataPack) out2.getMessageQueue().poll();
		System.out.println(data11.getSerial());
		System.out.println(data11.getDatas());

		Assert.assertEquals(data0.getSerial(), data11.getSerial());
		Assert.assertTrue(data0.getDatas() != null && !data0.getDatas().isEmpty());
		Assert.assertTrue(data11.getDatas() != null && !data11.getDatas().isEmpty());
		Assert.assertEquals(data0.getDatas().size(), data11.getDatas().size());

		for (int ii = 0; ii < data0.getDatas().size(); ii++) {
			ByteArrayOutputStream os1 = new ByteArrayOutputStream();
			ByteArrayOutputStream os2 = new ByteArrayOutputStream();
			ByteBuffer cc1 = data0.getDatas().get(ii);
			ByteBuffer cc2 = data11.getDatas().get(ii);
			os1.write(cc1.array(), cc1.position(), cc1.remaining());
			os2.write(cc2.array(), cc2.position(), cc2.remaining());
			String sss1 = Arrays.toString(os1.toByteArray());
			String sss2 = Arrays.toString(os2.toByteArray());
			System.out.println(sss1);
			System.out.println(sss2);
			Assert.assertEquals(sss1, sss2);
		}

	}

	@Test
	public void testHuge() throws Exception {
		int sz = 50000;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sz; i++) {
			sb.append("a");
		}
		NettyDataPack pack = getData(DemoService.class, "memory_setString",
		        new Object[] { "aaa", sb.toString(), 50000 });
		byte[] src = genBytes(pack);

		DummyNettyDecoder dec = new DummyNettyDecoder();
		ArrayList outter = new ArrayList();
		dec.decode(null, getByteBuf(src), outter);
		NettyDataPack data0 = (NettyDataPack) outter.get(0);
		System.out.println(data0.getSerial());
		System.out.println(data0.getDatas());

		AvroDecoder avdec = new AvroDecoder();
		DummyDecoderOutput out2 = new DummyDecoderOutput();
		IoBuffer inbuf2 = getIoBuffer(src);
		while (inbuf2.hasRemaining()) {
			avdec.decode(null, inbuf2, out2);
		}
		NettyDataPack data11 = (NettyDataPack) out2.getMessageQueue().poll();
		System.out.println(data11.getSerial());
		System.out.println(data11.getDatas());

		assertIt(data0, data11);
	}

	protected NettyDataPack getData(Class clz, String name, Object param) throws Exception {
		NettyDataPack pack = new NettyDataPack();
		pack.setSerial(Integer.MAX_VALUE);
		List<ByteBuffer> data1 = PackageTester.mock(clz, name, param);
		pack.setDatas(data1);
		return pack;
	}

	protected byte[] genBytes(NettyDataPack pack) throws Exception {
		ByteBuf buf = Unpooled.buffer(102400);
		ByteArrayOutputStream bosNetty = new ByteArrayOutputStream();
		DummyNettyEncode nenc = new DummyNettyEncode(bosNetty);
		nenc.encode(null, pack, buf);
		byte[] d1 = bosNetty.toByteArray();
		return d1;
	}

	protected ByteBuf getByteBuf(byte[] src) {
		ByteBuf inbuf = Unpooled.copiedBuffer(src);
		return inbuf;
	}

	protected IoBuffer getIoBuffer(byte[] src) {
		IoBuffer inbuf2 = IoBuffer.wrap(src);
		return inbuf2;
	}

	protected void assertIt(NettyDataPack data0, NettyDataPack data11) {
		Assert.assertEquals(data0.getSerial(), data11.getSerial());
		Assert.assertTrue(data0.getDatas() != null && !data0.getDatas().isEmpty());
		Assert.assertTrue(data11.getDatas() != null && !data11.getDatas().isEmpty());
		Assert.assertEquals(data0.getDatas().size(), data11.getDatas().size());

		for (int ii = 0; ii < data0.getDatas().size(); ii++) {
			ByteArrayOutputStream os1 = new ByteArrayOutputStream();
			ByteArrayOutputStream os2 = new ByteArrayOutputStream();
			ByteBuffer cc1 = data0.getDatas().get(ii);
			ByteBuffer cc2 = data11.getDatas().get(ii);
			os1.write(cc1.array(), cc1.position(), cc1.remaining());
			os2.write(cc2.array(), cc2.position(), cc2.remaining());
			String sss1 = Arrays.toString(os1.toByteArray());
			String sss2 = Arrays.toString(os2.toByteArray());
			System.out.println(sss1);
			System.out.println(sss2);
			Assert.assertEquals(sss1, sss2);
		}
	}

}

class DummyDecoderOutput extends AbstractProtocolDecoderOutput {

	@Override
	public void flush(NextFilter nextFilter, IoSession session) {

	}

}

class DummyNettyDecoder extends NettyFrameDecoder {

	@Override
	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		while (in.isReadable()) {
			super.decode(ctx, in, out);
		}
		NettyDataPack data11 = (NettyDataPack) out.get(0);
		for (int ii = 0; ii < data11.getDatas().size(); ii++) {
			ByteArrayOutputStream os1 = new ByteArrayOutputStream();
			ByteBuffer cc1 = data11.getDatas().get(ii);
			os1.write(cc1.array(), cc1.position(), cc1.remaining());
			String sss1 = Arrays.toString(os1.toByteArray());
			System.out.println(sss1);
			System.out.println("===> " + os1.toByteArray().length);
		}
	}

}