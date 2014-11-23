package com.duitang.service.codecs;

import java.io.ByteArrayOutputStream;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class AvroEncoder<T> extends OneToOneEncoder implements ChannelHandler {

	private DatumWriter<T> datumWriter;

	private BinaryEncoder reuse;

	public AvroEncoder(DatumWriter<T> writer, BinaryEncoder reuse) {
		this.datumWriter = writer;
		this.reuse = reuse;
	}

	/**
	 * Transforms the specified message into another message and return the
	 * transformed message. Note that you can not return {@code null}, unlike
	 * you can in
	 * {@link org.jboss.netty.handler.codec.oneone.OneToOneDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, Object)}
	 * ; you must return something, at least
	 * {@link org.jboss.netty.buffer.ChannelBuffers#EMPTY_BUFFER}.
	 */
	@Override
	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (msg instanceof GenericRecord) {

			// 取得消息正文
			T datum = (T) msg;

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, reuse);

			datumWriter.write(datum, encoder);

			encoder.flush();

			byte[] array = outputStream.toByteArray();

			return ctx.getChannel().getConfig().getBufferFactory().getBuffer(array, 0, array.length);

		}
		return msg;
	}

	public DatumWriter<T> getDatumWriter() {
		return datumWriter;
	}

	public BinaryEncoder getReuse() {
		return reuse;
	}

}
