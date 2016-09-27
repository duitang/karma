package com.duitang.service.karma.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.List;

public class KarmaNettyDecoder extends ByteToMessageDecoder {

  static AttributeKey<KarmaPacketDecoderNetty> DECODER_NAME = AttributeKey.valueOf("DECODER");

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (!in.isReadable()) {
      return;
    }
    Attribute<KarmaPacketDecoderNetty> dd = ctx.channel().attr(DECODER_NAME);
    KarmaPacketDecoderNetty dec = dd.get();
    if (dec == null) {
      dec = new KarmaPacketDecoderNetty();
      dd.set(dec);
    }
    dec.decode(ctx, in, out);
  }

}
