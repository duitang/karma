package com.duitang.service.karma.router;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.handler.RPCContext;
import com.duitang.service.karma.handler.RPCHandler;
import com.duitang.service.karma.meta.BinaryPacketData;
import com.duitang.service.karma.meta.BinaryPacketHelper;
import com.duitang.service.karma.meta.BinaryPacketRaw;

public class JavaRouter implements Router<BinaryPacketRaw> {

	final static public AttributeKey<Long> UUID_KEY = AttributeKey.valueOf("PING_UUID");
	protected RPCHandler handler;
	protected ExecutorService execPool = new ThreadPoolExecutor(5, 100, 300L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000));

	@Override
	public void setHandler(RPCHandler handler) {
		this.handler = handler;
	}

	@Override
	public void route(RPCContext ctx, BinaryPacketRaw raw) throws KarmaException {
		execPool.submit(new KarmaJobRunner(ctx, raw));
	}

	class KarmaJobRunner implements Runnable {

		RPCContext ctx;
		BinaryPacketRaw raw;

		public KarmaJobRunner(RPCContext ctx, BinaryPacketRaw rawPack) {
			this.ctx = ctx;
			this.raw = rawPack;
		}

		@Override
		public void run() {
			BinaryPacketData data = null;
			do {
				try {
					data = BinaryPacketHelper.fromRawToData(raw);
					if (BinaryPacketHelper.isPing(data)) {
						// fast return because of ping
						if (raw.ctx != null) {
							Attribute<Long> attr = raw.ctx.channel().attr(UUID_KEY);
							attr.set(raw.getUuid());
						}
						break;
					}
					ctx.name = data.domain;
					ctx.method = data.method;
					ctx.params = data.param;
					handler.lookUp(ctx);
					handler.invoke(ctx);
					data.ret = ctx.ret;
				} catch (Throwable e) {
					if (data == null) {
						data = new BinaryPacketData();
					}
					data.ex = e;
				}
			} while (false);
			if (raw.ctx != null) {
				Attribute<Long> attr = raw.ctx.channel().attr(UUID_KEY);
				Long uuid_checkpoint = attr.get();
				if (uuid_checkpoint != null && uuid_checkpoint >= data.uuid){
					// uuid before ping checkpoint should be discard
					return;
				}
				raw.ctx.writeAndFlush(data.getBytes());
			}
		}
	}
}
