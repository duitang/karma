package com.duitang.service.meta;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.mina.core.buffer.IoBuffer;

import com.duitang.service.KarmaException;

public class BinaryPacketData {

	public final static byte[] EMPTY = {};
	public final static byte[] MAGIC_CODE = { 11, 18 };

	public float version;
	public int flag;
	public long uuid;
	public RPCConfig conf;
	public String domain;
	public String method;
	public Object[] param;
	public Object ret;
	public Throwable ex;

	public IoBuffer getBytes() {
		IoBuffer buffer = IoBuffer.allocate(1024);
		buffer.setAutoExpand(true);
		byte[] w_bytes;
		int total = 0;
		buffer.put(MAGIC_CODE[0]);
		buffer.put(MAGIC_CODE[1]);
		int total_pos = buffer.position();

		buffer.putInt(0); // for total, take a seat
		buffer.putLong(0); // for checksum, take a seat

		// version
		buffer.putFloat(version);

		// flag
		buffer.putInt(flag);

		// uuid
		buffer.putLong(uuid);

		// config
		try {
			// at version 1 we ignore flag and using default
			w_bytes = objToBytes(conf);
		} catch (Throwable t) {
			w_bytes = EMPTY;
			if (ex == null) {
				ex = t;
			}
		}
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// domain
		w_bytes = domain.getBytes();
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// method
		w_bytes = method.getBytes();
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// parameter
		try {
			w_bytes = objToBytes(param);
		} catch (Throwable t) {
			w_bytes = EMPTY;
			if (ex == null) {
				ex = t;
			}
		}
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// return
		try {
			w_bytes = objToBytes(ret);
		} catch (Throwable e) {
			w_bytes = EMPTY;
			if (ex == null) {
				ex = e;
			}
		}
		buffer.putInt(w_bytes.length);
		buffer.put(w_bytes);

		// throwable
		try {
			w_bytes = objToBytes(ex);
		} catch (Throwable e) {
			w_bytes = EMPTY;
		}
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

	protected byte[] errToBytes(Throwable t) {
		if (t == null) {
			return EMPTY;
		}
		String ret = ExceptionUtils.getStackTrace(t);
		return ret.getBytes();
	}

}
