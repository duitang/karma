package com.duitang.service.karma.meta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.duitang.service.karma.KarmaException;

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

	public ByteBuf getBytes() {
		ByteBuf buffer = Unpooled.buffer();
		byte[] w_bytes;
		int total = 0;
		buffer.writeByte(MAGIC_CODE[0]);
		buffer.writeByte(MAGIC_CODE[1]);
		int total_pos = buffer.writerIndex();

		buffer.writeInt(0); // for total, take a seat
		buffer.writeLong(0); // for checksum, take a seat

		// version
		buffer.writeFloat(version);

		// flag
		buffer.writeInt(flag);

		// uuid
		buffer.writeLong(uuid);

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
		buffer.writeInt(w_bytes.length);
		buffer.writeBytes(w_bytes);

		// domain
		if (domain != null) {
			w_bytes = domain.getBytes();
		} else {
			w_bytes = EMPTY;
		}
		buffer.writeInt(w_bytes.length);
		buffer.writeBytes(w_bytes);

		// method
		if (method != null) {
			w_bytes = method.getBytes();
		} else {
			w_bytes = EMPTY;
		}
		buffer.writeInt(w_bytes.length);
		buffer.writeBytes(w_bytes);

		// parameter
		try {
			w_bytes = objToBytes(param);
		} catch (Throwable t) {
			w_bytes = EMPTY;
			if (ex == null) {
				ex = t;
			}
		}
		buffer.writeInt(w_bytes.length);
		buffer.writeBytes(w_bytes);

		// return
		try {
			w_bytes = objToBytes(ret);
		} catch (Throwable e) {
			w_bytes = EMPTY;
			if (ex == null) {
				ex = e;
			}
		}
		buffer.writeInt(w_bytes.length);
		buffer.writeBytes(w_bytes);

		// throwable
		try {
			w_bytes = objToBytes(ex);
		} catch (Throwable e) {
			w_bytes = EMPTY;
		}
		buffer.writeInt(w_bytes.length);
		buffer.writeBytes(w_bytes);

		// last we fill total and checksum
		total = buffer.writerIndex();
		buffer.writerIndex(total_pos);
		buffer.writeInt(total);
		Checksum ck = new Adler32();
		// magic_code + int
		ck.update(buffer.array(), total_pos - 2, 6);
		// buffer.writerIndex(total_pos + 4);
		buffer.writeLong(ck.getValue());

		return buffer.writerIndex(total);
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
