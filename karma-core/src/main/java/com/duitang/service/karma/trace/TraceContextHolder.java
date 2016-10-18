package com.duitang.service.karma.trace;

import java.util.LinkedList;

public class TraceContextHolder {

	final static int MAX_STACK_SIZE = 100;
	final static Long[] EMPTY = { null, null, null };
	static TracerSampler sampler = new AlwaysNotSampled();

	static class TraceStack {

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
	}

	public static Long[] snap() {
		TraceStack ctx = CURRENT.get();
		if (ctx.stack.isEmpty()) {
			return EMPTY;
		}
		TraceCell tc = ctx.stack.getFirst();
		return new Long[] { tc.traceId, tc.spanId, tc.sampled ? 1L : 0L };
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
		TraceCell last = newOne(isClient, id[0], id[1],
				id[2] == null ? TraceContextHolder.getSampler().sample() : (id[2] == 1L ? true : false));
		push(last);
		return last;
	}

	public static TraceCell release() {
		TraceStack ctx = CURRENT.get();
		if (ctx.stack.isEmpty()) {
			// should not be happen
			return newOne(true, null, null, false);
		}
		return ctx.stack.pop();
	}

	synchronized static public void setSampler(TracerSampler sampler) {
		TraceContextHolder.sampler = sampler;
	}

	synchronized static public void alwaysSampling() {
		TraceContextHolder.sampler = new AlwaysSampled();
	}

	synchronized static public void neverSampling() {
		TraceContextHolder.sampler = new AlwaysNotSampled();
	}

	static public TracerSampler getSampler() {
		return TraceContextHolder.sampler;
	}

	private static TraceCell newOne(boolean isClient, Long traceId, Long parentId, boolean sampled) {
		return new TraceCell(isClient, null, null, traceId, parentId, sampled, null, null, null);
	}

}
