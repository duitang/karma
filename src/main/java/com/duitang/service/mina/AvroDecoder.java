package com.duitang.service.mina;

import java.io.ByteArrayOutputStream;
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
	
	protected ByteArrayOutputStream os = new ByteArrayOutputStream(102400);
	protected int wanted = -1;

	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		System.out.println("----> " + in.remaining());
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

	private boolean decodePackHeader(IoSession session, IoBuffer buffer) throws Exception {
		if (buffer.buf().remaining() < 8) {
			return false;
		}

		int serial = buffer.getInt();
		int listSize = buffer.getInt();

		this.listSize = listSize;
		dataPack = new NettyDataPack();
		dataPack.setSerial(serial);
		dataPack.setDatas(new ArrayList<ByteBuffer>(listSize));
		os.reset();

		return true;
	}

	private boolean decodePackBody(IoSession session, IoBuffer buffer) throws Exception {
		if (buffer.remaining() < 4) {
			return false;
		}

		buffer.mark();

		int length = buffer.getInt();

		if (buffer.remaining() < length) {
			System.out.println("===========>" + length);
//			buffer.reset();
			os.write(buffer.array(), buffer.position(), buffer.remaining());
			buffer.position(buffer.position() + buffer.remaining());
			return false;
		}

		ByteBuffer bb = ByteBuffer.wrap(buffer.array(), buffer.position(), length);
		buffer.position(buffer.position() + length);
		dataPack.getDatas().add(bb);

		os.reset();
		System.out.println("add pack: " + bb.remaining() + ", @" + dataPack.getDatas().size() + " ,list=" + listSize);

		return dataPack.getDatas().size() == listSize;
	}

}
