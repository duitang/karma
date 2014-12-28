package com.duitang.service.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import com.duitang.service.meta.BinaryPacketData;
import com.duitang.service.meta.BinaryPacketRaw;

public class KarmaPacketDecoderNetty {

	// check:
	// magic_code(2) + total(4) + checksum(8) + float(4) + flag(4) + uuid(8)
	static final int HEADER = 30;

	protected BinaryPacketRaw rawPack;
	protected int state = 0;
	protected int[] szBuf = new int[1];

	public void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {
		ByteBuffer buf;
		boolean flag = true; // force return
		while (in.readableBytes() > 1 && flag) {
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
				out.add(rawPack);
				rawPack = null;
				state = 0;
			}
		}
	}

	protected BinaryPacketRaw setUpRawPacket(ByteBuf in) {
		if (in.readableBytes() < HEADER) {
			return null;
		}
		int pos = in.readerIndex();
		byte[] magic = { in.readByte(), in.readByte() };
		if (magic[0] == BinaryPacketData.MAGIC_CODE[0] && magic[1] == BinaryPacketData.MAGIC_CODE[1]) {
			in.markReaderIndex(); // mark start
			// magic code
			int total = in.readInt();
			long cksum = in.readLong();
			float version = in.readFloat();
			int flag = in.readInt();
			long uuid = in.readLong();
			int pos2 = in.readerIndex();
			Checksum ck = new Adler32();
			byte[] ttt = new byte[6];
			in.readerIndex(pos);
			in.readBytes(ttt);
			in.readerIndex(pos2);
			ck.update(ttt, 0, 6);
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
			in.resetReaderIndex(); // just skip, try later
		}
		// not magic code? just skip, try later
		return null;
	}

	protected boolean readSize(ByteBuf in, int[] size) {
		if (in.readableBytes() < 4) { // if long ready
			return false;
		}
		size[0] = in.readInt();
		state++;
		return true;
	}

	protected boolean readBytes(ByteBuf in, int wanted, ByteBuffer buf) {
		if (wanted == 0) { // empty field
			state++;
			return true;
		}
		int left = wanted - buf.position();
		if (left == 0) { // all done
			state++;
			return true;
		}
		if (!in.isReadable()) { // read out
			return false;
		}
		int readit = left <= in.readableBytes() ? left : in.readableBytes();
		boolean ret = left <= in.readableBytes();
		in.readBytes(buf.array(), buf.position(), readit);
		// buf.put(in.array(), in.readerIndex(), readit);
		// in.readerIndex(in.readerIndex() + readit);
		buf.position(buf.position() + readit);
		if (ret) {
			buf.flip();
			state++;
		}
		return ret;
	}

}
