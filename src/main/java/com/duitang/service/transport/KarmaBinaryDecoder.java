package com.duitang.service.transport;

import java.nio.ByteBuffer;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.meta.BinaryPacketRaw;

public class KarmaBinaryDecoder extends ProtocolDecoderAdapter {

	// check:
	// magic_code(2) + total(4) + checksum(8) + float(4) + flag(4) + uuid(8)
	static final int HEADER = 30;

	protected BinaryPacketRaw rawPack;
	protected int state = 0;
	protected int[] szBuf = new int[1];

	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		ByteBuffer buf;
		boolean flag = true; // force return
		while (in.remaining() > 1 && flag) {
			if (rawPack == null) {
				rawPack = setUpRawPacket(in);
				if (rawPack != null) { // built already
					state = 3;
				}
				continue; // anyway check if has remaining
			}
			switch (state) {
			case 3: // CONFIG_SIZE
				flag = readSize(in, szBuf);
				if (flag) {
					rawPack.setSzConf(szBuf[0]);
				}
				break;
			case 4: // CONFIG
				if (rawPack.getConf() == null && rawPack.getSzConf() > 0) {
					rawPack.setConf(ByteBuffer.allocate(rawPack.getSzConf()));
				}
				buf = rawPack.getConf();
				flag = readBytes(in, rawPack.getSzConf(), buf);
				break;
			case 5: // DOMAIN_SIZE
				flag = readSize(in, szBuf);
				if (flag) {
					rawPack.setSzDomainName(szBuf[0]);
				}
				break;
			case 6: // DOMAIN
				if (rawPack.getDomainName() == null && rawPack.getSzDomainName() > 0) {
					rawPack.setDomainName(ByteBuffer.allocate(rawPack.getSzDomainName()));
				}
				buf = rawPack.getDomainName();
				flag = readBytes(in, rawPack.getSzDomainName(), buf);
				break;
			case 7: // METHOD_SIZE
				flag = readSize(in, szBuf);
				if (flag) {
					rawPack.setSzMethodName(szBuf[0]);
				}
				break;
			case 8: // METHOD
				if (rawPack.getMethodName() == null && rawPack.getSzMethodName() > 0) {
					rawPack.setMethodName(ByteBuffer.allocate(rawPack.getSzMethodName()));
				}
				buf = rawPack.getMethodName();
				flag = readBytes(in, rawPack.getSzMethodName(), buf);
				break;
			case 9: // PARAMETER_SIZE
				flag = readSize(in, szBuf);
				if (flag) {
					rawPack.setSzParameter(szBuf[0]);
				}
				break;
			case 10: // PARAMETER
				if (rawPack.getParameter() == null && rawPack.getSzParameter() > 0) {
					rawPack.setParameter(ByteBuffer.allocate(rawPack.getSzParameter()));
				}
				buf = rawPack.getParameter();
				flag = readBytes(in, rawPack.getSzParameter(), buf);
				break;
			case 11: // RETURN_SIZE
				flag = readSize(in, szBuf);
				if (flag) {
					rawPack.setSzRet(szBuf[0]);
				}
				break;
			case 12: // RETURN
				if (rawPack.getRet() == null && rawPack.getSzRet() > 0) {
					rawPack.setRet(ByteBuffer.allocate(rawPack.getSzRet()));
				}
				buf = rawPack.getRet();
				flag = readBytes(in, rawPack.getSzRet(), buf);
				break;
			case 13: // EXCEPTION_SIZE
				// last ensure field
				flag = readSize(in, szBuf);
				if (flag) {
					rawPack.setSzError(szBuf[0]);
					if (szBuf[0] == 0) {
						state = 15;
					}
				}
				break;
			case 14: // EXCEPTION
				if (rawPack.getError() == null) {
					rawPack.setError(ByteBuffer.allocate(rawPack.getSzError()));
				}
				buf = rawPack.getError();
				flag = readBytes(in, rawPack.getSzError(), buf);
				break;
			default:
				break;
			}
			if (state == 15) { // end
				out.write(rawPack);
				rawPack = null;
				state = 0;
			}
		}
	}

	protected BinaryPacketRaw setUpRawPacket(IoBuffer in) {
		if (in.remaining() < HEADER) {
			return null;
		}
		int pos = in.position();
		byte[] magic = { in.get(), in.get() };
		if (magic[0] == BinaryPacketData.MAGIC_CODE[0] && magic[1] == BinaryPacketData.MAGIC_CODE[1]) {
			in.mark(); // mark start
			// magic code
			int total = in.getInt();
			long cksum = in.getLong();
			float version = in.getFloat();
			int flag = in.getInt();
			long uuid = in.getLong();
			Checksum ck = new Adler32();
			ck.update(in.array(), pos, 6);
			if (cksum == ck.getValue()) {
				// bingo
				BinaryPacketRaw ret = new BinaryPacketRaw();
				ret.setTotal(total);
				ret.setVersion(version);
				ret.setFlag(flag);
				ret.setUuid(uuid);
				return ret;
			}
			// sticky! invalid packet
			// ignore this magic code
			in.reset(); // just skip, try later
		}
		// not magic code? just skip, try later
		return null;
	}

	protected boolean readSize(IoBuffer in, int[] size) {
		if (in.remaining() < 4) { // if long ready
			return false;
		}
		size[0] = in.getInt();
		state++;
		return true;
	}

	protected boolean readBytes(IoBuffer in, int wanted, ByteBuffer buf) {
		if (wanted == 0) { // empty field
			state++;
			return true;
		}
		int left = wanted - buf.position();
		if (left == 0) { // all done
			state++;
			return true;
		}
		if (!in.hasRemaining()) { // read out
			return false;
		}
		int readit = left <= in.remaining() ? left : in.remaining();
		boolean ret = left <= in.remaining();
		buf.put(in.array(), in.position(), readit);
		in.position(in.position() + readit);
		if (ret) {
			buf.flip();
			state++;
		}
		return ret;
	}

}
