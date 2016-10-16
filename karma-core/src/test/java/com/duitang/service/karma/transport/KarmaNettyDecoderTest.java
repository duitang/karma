package com.duitang.service.karma.transport;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.duitang.service.demo.IDemoService;
import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;
import com.duitang.service.karma.meta.RPCConfig;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class KarmaNettyDecoderTest {

	static String outfilename = "/tmp/KarmaBinaryDecoderTest.dat";
	static BinaryPacketData ddd;
	static byte[] ddd_buf;
	static AtomicInteger checkLock = new AtomicInteger();
	static AtomicBoolean finished = new AtomicBoolean();

	@Before
	public void setUp() throws Exception {
		ddd = new BinaryPacketData();
		ddd.uuid = 123456789;
		ddd.domain = IDemoService.class.getName();
		ddd.method = "memory_getString";
		ddd.flag = 1;
		ddd.version = 1;
		ddd.conf = new RPCConfig();
		ddd.param = new Object[] { "aaa" };
		ByteBuf buf = ddd.getBytes();
		FileOutputStream fos = new FileOutputStream(outfilename);
		fos.write(buf.array(), buf.arrayOffset(), buf.readableBytes());
		fos.close();
		FileInputStream fis = new FileInputStream(outfilename);
		// tough
		ddd_buf = new byte[fis.available()];
		fis.read(ddd_buf);
		fis.close();
		buf.readByte();
		buf.readByte();
		System.out.println(buf.readInt());
		System.out.println(buf.readLong());
		System.out.println(buf.readFloat());
		System.out.println(buf.readInt());
		System.out.println(buf.readLong());
		int sz = buf.readInt();
		System.out.println(sz);
		byte[] bbb = new byte[sz];
		buf.readBytes(bbb);
		System.out.println(new String(bbb));
		sz = buf.readInt();
		System.out.println(sz);
		bbb = new byte[sz];
		buf.readBytes(bbb);
		System.out.println(new String(bbb));
		System.out.println("aa");
	}

	// @Test
	public void test1() throws Exception {
		ByteBuf in = Unpooled.wrappedBuffer(ddd_buf);
		KarmaPacketDecoderNetty dec = new KarmaPacketDecoderNetty();
		ArrayList lst = new ArrayList();
		dec.decode(null, in, lst);
		System.out.println(lst);
		BinaryPacketData dat = BinaryPacketHelper.fromRawToData((BinaryPacketRaw) lst.get(0));
		System.out.println(dat);
	}

	/**
	 * cut off inputstream to N tcp packet
	 *
	 * <pre>
	 * e.g. for 140 bytes binary packet
	 * packet: [43, 4, 4, 4, 4, 9, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4]
	 * packet: [29, 20, 12, 19, 7, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4]
	 * packet: [39, 77, 6, 11, 7]
	 * </pre>
	 */
	@Test
	public void test2() throws Exception {
		BinaryPacketData demodata = new BinaryPacketData();
		demodata.domain = IDemoService.class.getName();
		demodata.method = "memory_getString";
		demodata.uuid = 123456789L;
		demodata.flag = 1;
		demodata.version = 2.0f;
		demodata.conf = new RPCConfig();
		demodata.param = new Object[] { "aaa" };
		testNTcpPacket(demodata, 100);
	}

	/**
	 * variable content size
	 */
	@Test
	public void test3() throws Exception {
		Random rnd = new Random();
		for (int i = 0; i < 100; i++) {
			BinaryPacketData demodata = new BinaryPacketData();
			demodata.domain = genStr(rnd.nextInt(1000));
			demodata.method = genStr(rnd.nextInt(1000));
			demodata.uuid = rnd.nextLong();
			demodata.version = rnd.nextFloat();
			demodata.flag = rnd.nextInt();
			demodata.param = new Object[] { genStr(rnd.nextInt(1000)) };
			testNTcpPacket(demodata, 100);
		}
	}

	static String genStr(int size) {
		Random rnd = new Random();
		char a = 'a';
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size + 1; i++) {
			sb.append((char) ((int) a + rnd.nextInt(26)));
		}
		return sb.toString();
	}

	public void testNTcpPacket(BinaryPacketData demodata, int loop) throws Exception {
		ByteBuf bbbb = demodata.getBytes();
		byte[] dbuf = new byte[bbbb.readableBytes()];
		bbbb.getBytes(bbbb.readerIndex(), dbuf);

		checkLock = new AtomicInteger(0);
		int total = dbuf.length;
		int firstHead = KarmaPacketDecoderNetty.HEADER;
		int max_it = ((total - firstHead) / 4) + 1;
		Random rnd = new Random();
		for (int i = 0; i < loop; i++) {
			ArrayList lst = new ArrayList();
			int seg = rnd.nextInt(max_it) + 1; // ensure > 0
			int left = total;
			int[] seg_size = new int[seg];
			seg_size[0] = firstHead;
			left -= seg_size[0];
			for (int j = 1; j < seg_size.length; j++) {
				seg_size[j] = 4;
				left -= 4;
			} // ensure every seg_size >=1
			while (left > 0) {
				for (int j = 0; j < seg; j++) {
					int l = rnd.nextInt(left + 1);
					seg_size[j] += l;
					left -= l;
					if (left == 0) {
						break;
					}
				}
			}
			int check_size = 0;
			for (int j = 0; j < seg_size.length; j++) {
				check_size += seg_size[j];
			}
			Assert.assertEquals(check_size, total);
			// seg_size = new int[] { 52, 14, 6, 5, 7, 4, 4, 4, 4, 4, 4, 4, 4,
			// 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 };
			System.out.println("[" + i + "] Starting decodeing: " + Arrays.toString(seg_size));
			KarmaPacketDecoderNetty dec = new KarmaPacketDecoderNetty();
			KarmaNettyDecoderTest.finished.set(false);
			ByteBuf buffer = null;
			int pos = 0;
			int delta = 0;
			lst.clear();
			for (int j = 0; j < seg_size.length; j++) {
				buffer = Unpooled.wrappedBuffer(dbuf, pos - delta, seg_size[j] + delta);
				pos += seg_size[j];
				dec.decode(null, buffer, lst);
				delta = 0;
				if (buffer.isReadable()) {
					delta = buffer.readableBytes();
				}
			}
			checkit(demodata, lst.get(0));
			Assert.assertEquals(true, KarmaNettyDecoderTest.finished.get());
		}
		Assert.assertEquals(loop, checkLock.get());
	}

	// @Test
	public void testPacket() throws Exception {
		List<String> names = new ArrayList<String>();
		names.add("贡献度");
		names.add("影响力");
		names.add("活跃度");
		String id = "549bf6df0cf22cbd4d6394f1";
		BinaryPacketData data = new BinaryPacketData();
		data.domain = "com.duitang.service.biz.IClubMemberBoardService";
		data.method = "showMemberBoard";
		data.param = new Object[] { names, id };
		ByteBuf buffer;
		buffer = data.getBytes();

		List lst = new ArrayList();
		KarmaPacketDecoderNetty dec = new KarmaPacketDecoderNetty();
		dec.decode(null, buffer, lst);

		// ByteBuffer.allocate(-1);
	}

	static void checkit(BinaryPacketData data, Object message) {
		BinaryPacketRaw buf = (BinaryPacketRaw) message;
		BinaryPacketData data1 = null;
		try {
			data1 = BinaryPacketHelper.fromRawToData(buf);
		} catch (KarmaException e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertEquals(data.flag, data1.flag);
		Assert.assertTrue(data.version == data1.version);
		// Assert.assertEquals(data.conf.toString(), data1.conf.toString());
		Assert.assertEquals(data.domain, data1.domain);
		Assert.assertEquals(data.method, data1.method);
		Assert.assertEquals(data.param.length, data1.param.length);
		Assert.assertEquals(data.param[0], data1.param[0]);
		System.out.println("all field assert!");
		KarmaNettyDecoderTest.checkLock.incrementAndGet();
		KarmaNettyDecoderTest.finished.set(true);
	}

}
