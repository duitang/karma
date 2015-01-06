package com.duitang.service.karma.meta;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.mina.core.buffer.IoBuffer;
import org.junit.Test;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.demo.DemoService;

public class BinaryPacketHelperTest {

	@Test
	public void test() {
		long val = 0;
		long ts = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			Checksum ck = new Adler32();
			byte[] text = "1111122222".getBytes();
			ck.update(text, 0, text.length);
			val = ck.getValue();
			// System.out.println(val);
		}
		ts = System.currentTimeMillis() - ts;
		System.out.println(ts);
		System.out.println(val);

		long a = 1L;
		long b = Long.MAX_VALUE;
		long c = Long.MIN_VALUE;

		ByteBuffer bb = ByteBuffer.allocate(1024);
		System.out.println("....");
		System.out.println(bb.position());
		bb.putLong(a);
		System.out.println(bb.position());
		bb.putLong(b);
		System.out.println(bb.position());
		bb.putLong(c);
		System.out.println(bb.position());
		byte ch1 = 11;
		byte ch2 = 18;
		bb.put(ch1);
		bb.put(ch2);
		System.out.println(bb.position());
		System.out.println("----");

		bb.put(new byte[0]);
		System.out.println(bb.position());

		bb.putLong(11, 222);
		System.out.println(bb.position());

		IoBuffer buf = IoBuffer.allocate(1024);

		buf.putLong(1111);
		System.out.println(buf.position());
		System.out.println(buf.arrayOffset());

		byte[] src = new byte[] { 12, 18, 0, 0, 0, 0, 0, 0, 0, (byte) 170 };
		// System.out.println((byte) 170);
		Checksum ck = new Adler32();
		ck.update(src, 0, src.length);
		System.out.println(ck.getValue());

	}

	@Test
	public void test1() throws KarmaException, IOException {
		BinaryPacketData d = new BinaryPacketData();
		d.domain = DemoService.class.getName();
		d.method = "memory_getString";
		d.flag = 0;
		d.param = new Object[] { "aaa" };
		IoBuffer buf = IoBuffer.wrap(d.getBytes().nioBuffer());
		FileOutputStream fos = new FileOutputStream("/tmp/a.dat");
		fos.write(buf.array(), buf.arrayOffset(), buf.limit());
		fos.close();
	}

}
