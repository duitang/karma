package com.duitang.service.mina;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.ipc.Callback;

import com.duitang.service.base.CallbackRepository;

public class CallbackCenter implements CallbackRepository {

	final protected static CallbackCenter allinone = new CallbackCenter();

	protected final AtomicInteger serialGenerator = new AtomicInteger(0);
	protected final Map<Integer, Callback<List<ByteBuffer>>> requests = new ConcurrentHashMap<Integer, Callback<List<ByteBuffer>>>();

	static public CallbackCenter getInstance() {
		return allinone;
	}

	private CallbackCenter() {
	}

	@Override
	public void push(int cbid, Callback<List<ByteBuffer>> callback) {
		if (callback == null) {
			return;
		}
		requests.put(cbid, callback);
	}

	@Override
	public Callback<List<ByteBuffer>> pop(int cbid) {
		return requests.remove(cbid);
	}

	@Override
	public int genId(List<ByteBuffer> data, Callback<List<ByteBuffer>> callback) {
		return serialGenerator.incrementAndGet();
	}

}
