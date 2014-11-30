package com.duitang.service.mina;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class AvroDecoder extends ProtocolDecoderAdapter {

	private boolean packHeaderRead = false;
	private int listSize;
	private NettyDataPack dataPack;

	protected ByteBuffer tmpbuf;
	protected int wanted = -1;

	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// System.out.println("----> " + in.remaining());

		while (in.hasRemaining()) {
			if (!packHeaderRead) {
				if (decodePackHeader(session, in)) {
					packHeaderRead = true;
				}
			} else {
				if (decodePackBody(session, in)) {
					packHeaderRead = false; // reset state
					out.write(dataPack);
				} else {
				}
			}
		}
	}

	private boolean decodePackHeader(IoSession session, IoBuffer buffer) throws Exception {
		if (buffer.buf().remaining() < 8) {
			return false;
		}

		int serial = buffer.getInt();
		int listSize = buffer.getInt();

		// System.out.println("serial ====> " + serial);
		// System.out.println("listsize ====> " + listSize);
		this.listSize = listSize;
		dataPack = new NettyDataPack();
		dataPack.setSerial(serial);
		dataPack.setDatas(new ArrayList<ByteBuffer>(listSize));

		wanted = -1;

		return true;
	}

	private boolean decodePackBody(IoSession session, IoBuffer buffer) throws Exception {
		if (buffer.remaining() < 4) {
			return false;
		}

		if (wanted < 0) {
			wanted = buffer.getInt();
			tmpbuf = ByteBuffer.allocate(wanted);
		}

		if (buffer.remaining() >= wanted) {
			// direct wrapper it
			tmpbuf.put(buffer.array(), buffer.position(), wanted).flip();
			buffer.position(buffer.position() + wanted);
			dataPack.getDatas().add(tmpbuf);
			wanted = -1;
			// System.out.println("}}}}}}}}}}}}}}=>" +
			// Boolean.valueOf(dataPack.getDatas().size() == listSize));
			return dataPack.getDatas().size() == listSize;
		}

		// partial read

		int readit = buffer.remaining();
		tmpbuf.put(buffer.array(), buffer.position(), readit);
		buffer.position(buffer.position() + readit);
		wanted -= readit;

		// System.out.println("add pack: " + bb.remaining() + ", @" +
		// dataPack.getDatas().size() + " ,list=" + listSize);

		return false;
	}
}
