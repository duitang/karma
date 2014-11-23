package com.duitang.service.codecs;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.jboss.netty.buffer.ChannelBuffer;


/**
 * <pre>
 * 
 * Created by IntelliJ IDEA.
 * User: zhenqin
 * Date: 13-8-9
 * Time: 上午9:33
 * To change this template use File | Settings | File Templates.
 * 
 * </pre>
 * 
 * @author zhenqin
 */
public class AvroDecoder<T> extends ChannelInboundHandlerAdapter {

	/**
	 * Avro Data Reader
	 */
	private DatumReader<T> datumReader;

	/**
	 * Decoder
	 */
	private BinaryDecoder reuse;

	/**
	 * 
	 * @param datumReader
	 *            Avro Schema Reader
	 * @param reuse
	 *            可为null
	 */
	public AvroDecoder(DatumReader<T> datumReader, BinaryDecoder reuse) {
		this.datumReader = datumReader;
		this.reuse = reuse;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return;
		}

		ChannelBuffer buf = (ChannelBuffer) msg;
		final byte[] array;
		final int offset;
		final int length = buf.readableBytes();

		if (buf.hasArray()) {
			array = buf.array();
			offset = buf.arrayOffset() + buf.readerIndex();
		} else {
			array = new byte[length];
			buf.getBytes(buf.readerIndex(), array, 0, length);
			offset = 0;
		}

		Decoder decoder = DecoderFactory.get().binaryDecoder(array, offset, length, reuse);
		GenericRecord result = (GenericRecord) datumReader.read(null, decoder);
		return;
	}

	public DatumReader<T> getDatumReader() {
		return datumReader;
	}

	public BinaryDecoder getReuse() {
		return reuse;
	}

}