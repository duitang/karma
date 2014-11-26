package com.duitang.service.base;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.avro.ipc.Callback;

public interface CallbackRepository {

	public int genId(List<ByteBuffer> data, Callback<List<ByteBuffer>> callback);

	/**
	 * dirty work but ok now
	 * 
	 * @param data
	 * @param callback
	 */
	public void push(int cbid, Callback<List<ByteBuffer>> callback);

	public Callback<List<ByteBuffer>> pop(int cbid);

}
