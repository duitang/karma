package com.duitang.service.meta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.mina.core.buffer.IoBuffer;

import com.duitang.service.KarmaException;

public class BinaryPacketData {

	public final static byte[] EMPTY = new byte[0];
	public final static byte[] MAGIC_CODE = { 11, 18 };

	public int flag;
	public long uuid;
	public String domain;
	public String method;
	public Object[] param;
	public Object ret;
	public Throwable ex;

	public IoBuffer getBytes() throws KarmaException {
		IoBuffer buffer = IoBuffer.allocate(1024);
		buffer.setAutoExpand(true);
		byte[] w_bytes;
		int total = 0;
		buffer.put(MAGIC_CODE[0]);
		buffer.put(MAGIC_CODE[1]);
		int total_pos = buffer.position();

		buffer.putInt(0); // for total, take a seat
		buffer.putLong(0); // for checksum, take a seat

		// flag
		buffer.putInt(flag);

		// uuid
		buffer.putLong(uuid);

		// domain
		w_bytes = domain.getBytes();
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// method
		w_bytes = method.getBytes();
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// parameter
		w_bytes = objToBytes(param);
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// return
		w_bytes = objToBytes(ret);
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// throwable
		w_bytes = objToBytes(ex);
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// last we fill total and checksum
		total = buffer.position();
		buffer.putInt(total_pos, total);
		Checksum ck = new Adler32();
		// magic_code + long
		ck.update(buffer.array(), total_pos - 2, 6);
		buffer.putLong(total_pos + 4, ck.getValue());

		return buffer.flip();
	}

	protected byte[] objToBytes(Object src) throws KarmaException {
		if (src == null) {
			return EMPTY;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(src);
			return bos.toByteArray();
		} catch (IOException e) {
			throw new KarmaException(e);
		}
	}

}
