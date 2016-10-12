package com.duitang.service.karma.trace;

import java.util.LinkedList;

public class TraceContextHolder {

	final static int MAX_STACK_SIZE = 100;
	final static Long[] EMPTY = { null, null };

	static class TraceStack {

		TracerSampler sampler = new AlwaysNotSampled();
		boolean sampled = sampler.sample();
		LinkedList<TraceCell> stack = new LinkedList<TraceCell>();

	}

	private static final ThreadLocal<TraceStack> CURRENT = new ThreadLocal<TraceStack>() {

		@Override
		protected TraceStack initialValue() {
			return new TraceStack();
		}

	};

	public static void reset() {
		TraceStack it = CURRENT.get();
		it.stack.clear();
		it.sampled = it.sampler.sample();
	}

	public static void reset(String clazzName, String method, Object[] params) {
		TraceStack it = CURRENT.get();
		it.stack.clear();
		it.sampled = it.sampler.sample(clazzName, method, params);
	}

	public static Long[] snap() {
		TraceStack ctx = CURRENT.get();
		if (ctx.stack.isEmpty()) {
			return EMPTY;
		}
		TraceCell tc = ctx.stack.getFirst();
		return new Long[] { tc.traceId, tc.spanId };
	}

	public static boolean sampled() {
		return CURRENT.get().sampled;
	}

	public static void push(TraceCell tc) {
		TraceStack ctx = CURRENT.get();
		ctx.stack.push(tc);
		if (ctx.stack.size() >= MAX_STACK_SIZE) {
			// discard
			ctx.stack.removeLast(); // overflow header
		}
	}

	public static TraceCell accquire(boolean isClient) {
		Long[] id = snap();
		TraceCell last = newOne(isClient, id[0], id[1], CURRENT.get().sampled);
		push(last);
		return last;
	}

	public static TraceCell release() {
		TraceStack ctx = CURRENT.get();
		if (ctx.stack.isEmpty()) {
			// should not be happen
			return newOne(true, null, null, ctx.sampled);
		}
		return ctx.stack.pop();
	}

	static public void setSampler(TracerSampler sampler) {
		CURRENT.get().sampler = sampler;
	}

	private static TraceCell newOne(boolean isClient, Long traceId, Long parentId, boolean sampled) {
		return new TraceCell(isClient, null, null, traceId, parentId, sampled, null, null, null);
	}

}
