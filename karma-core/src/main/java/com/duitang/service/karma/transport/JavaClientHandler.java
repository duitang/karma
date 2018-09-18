package com.duitang.service.karma.transport;

import com.duitang.service.karma.client.KarmaRemoteLatch;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

public class JavaClientHandler extends SimpleChannelInboundHandler<BinaryPacketRaw> {

  final static protected Logger error = LoggerFactory.getLogger(JavaClientHandler.class);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, BinaryPacketRaw msg) throws Exception {
    Attribute<KarmaRemoteLatch> att = ctx.channel().attr(KarmaRemoteLatch.LATCH_KEY);
    KarmaRemoteLatch latch = att.get();
    BinaryPacketData data = null;
    try {
        data = BinaryPacketHelper.fromRawToData(msg);
    } catch (KarmaException e) {
        // catch the exception from BinaryPacketHelper.bufToObj
        latch.offerError(e);
        return;
    }
    if (latch.getUuid() != data.uuid) {
      // mismatch uuid is likely due to timeout request. duplicate with timeout error,
      // so just warn it.
      error.warn("wanted uuid => " + latch.getUuid() + ", got uuid => " + data.uuid);
      return;
    }

    if (data.ex != null) {
      latch.offerError(data.ex);
    } else {
      latch.offerResult(data.ret);
    }
  }
}
