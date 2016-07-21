package com.duitang.service.karma.transport;

import com.duitang.service.karma.client.KarmaRemoteLatch;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;
import com.duitang.service.karma.support.CCT;
import com.duitang.service.karma.support.TraceChainDO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

public class JavaClientHandler extends SimpleChannelInboundHandler<BinaryPacketRaw> {

  final static protected Logger error = LoggerFactory.getLogger(JavaClientHandler.class);

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, BinaryPacketRaw msg) throws Exception {
    BinaryPacketData data = BinaryPacketHelper.fromRawToData(msg);
    Attribute<KarmaRemoteLatch> att = ctx.channel().attr(KarmaRemoteLatch.LATCH_KEY);
    KarmaRemoteLatch latch = att.get();
    if (latch.getUuid() != data.uuid) {
      // mismatch uuid is likely due to timeout request. duplicate with timeout error,
      // so just warn it.
      error.warn("wanted uuid => " + latch.getUuid() + ", got uuid => " + data.uuid);
      return;
    }
    if (data.conf != null && data.conf.getConf(CCT.RPC_CONF_KEY) != null) {
      CCT.mergeTraceChain((TraceChainDO) data.conf.getConf(CCT.RPC_CONF_KEY));
    }
    if (data.ex != null) {
      latch.offerError(data.ex);
    } else {
      latch.offerResult(data.ret);
    }
  }
}
