package com.duitang.service.karma.meta;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

import com.duitang.service.karma.KarmaException;

final public class BinaryPacketHelper {

	private BinaryPacketHelper() {
	};

	static public BinaryPacketData fromRawToData(BinaryPacketRaw raw) throws KarmaException {
		BinaryPacketData ret = new BinaryPacketData();
		ret.flag = raw.flag;
		ret.version = raw.version;
		// current version 1 using java default
		ret.conf = (RPCConfig) bufToObj(raw.conf);
		ret.uuid = raw.uuid;
		ret.domain = bufToStr(raw.domainName);
		ret.method = bufToStr(raw.methodName);
		ret.param = (Object[]) bufToObj(raw.parameter);
		if (ret.param == null) {
			ret.param = new Object[0];
		}
		ret.ret = bufToObj(raw.ret);
		ret.ex = (Throwable) bufToObj(raw.error);
		return ret;
	}

	static private String bufToStr(ByteBuffer buf) {
		if (buf == null) {
			return null;
		}
		return new String(buf.array(), buf.arrayOffset(), buf.remaining());
	}

	static private Object bufToObj(ByteBuffer buf) throws KarmaException {
		if (buf == null) {
			return null;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(buf.array(), buf.arrayOffset(), buf.remaining());
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bis);
			return ois.readObject();
		} catch (Exception e) {
			throw new KarmaException(e);
		}
	}

//	static private KarmaException bufToEx(ByteBuffer buf) {
//		if (buf == null) {
//			return null;
//		}
//		String ret = new String(buf.array(), buf.arrayOffset(), buf.remaining());
//		return new KarmaException(ret);
//	}

}
