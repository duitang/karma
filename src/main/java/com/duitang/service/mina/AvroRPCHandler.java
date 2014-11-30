package com.duitang.service.mina;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.avro.ipc.Callback;
import org.apache.avro.ipc.NettyTransportCodec.NettyDataPack;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duitang.service.base.CallbackRepository;

public class AvroRPCHandler extends IoHandlerAdapter {

	public static boolean debugMode = false;
	public static int debugOutputCount = 10000;
	public static String dumpPath = "/tmp";
	static protected Logger logger = LoggerFactory.getLogger(AvroRPCHandler.class);

	protected CallbackRepository cbcenter = CallbackCenter.getInstance();
	protected Executor actionPool = Executors.newFixedThreadPool(200);

	public AvroRPCHandler(CallbackRepository cbcenter) {
		this.cbcenter = cbcenter;
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		NettyDataPack dataPack = (NettyDataPack) message;
		Callback<List<ByteBuffer>> callback = cbcenter.pop(dataPack.getSerial());
		if (callback == null) {
			if (debugMode) {
				if (logger.isDebugEnabled()) {
					logger.debug("Missing previous call info = " + dataPack.getSerial());
				}
			}
			System.out.println(cbcenter);
			throw new RuntimeException("Missing previous call info " + dataPack.getSerial());
		}
		actionPool.execute(new WrapperCallback(callback, dataPack));
		// new WrapperCallback(callback, dataPack).run();
	}

	class WrapperCallback implements Runnable {

		Callback<List<ByteBuffer>> cb;
		NettyDataPack data;

		public WrapperCallback(Callback<List<ByteBuffer>> cb, NettyDataPack data) {
			this.cb = cb;
			this.data = data;
		}

		@Override
		public void run() {
			try {
				if (debugMode) {
					int id = data.getSerial();
					if (id % debugOutputCount == 0) {
						System.out.println("total package recieved .... " + id);
					}
					dumpIt(data.getSerial(), data);
				}
				// System.out.println("with ====> " + data.getSerial());
				cb.handleResult(data.getDatas());
				if (debugMode) {
					int id = data.getSerial();
					if (id % debugOutputCount == 0) {
						System.out.println("total package done .... " + id);
					}
				}
			} catch (Exception e) {
				if (debugMode) {
					logger.debug("total package failure .... " + data.getSerial(), e);
				}
			}
		}
	}

	protected void dumpIt(int serial, NettyDataPack datapack) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(dumpPath, String.valueOf(serial) + ".dat"));
			for (ByteBuffer bb : datapack.getDatas()) {
				try {
					fos.write(bb.array(), bb.position(), bb.remaining());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
